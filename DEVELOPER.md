# Jadaptive Developer Manual

This guide explains how to build entities, fields, services, and custom UI pages in the Jadaptive framework. It expands on AGENT.md with narrative guidance and code examples.

## Entities and templates

Entities are annotated POJOs stored in MongoDB. An `ObjectTemplate` is generated from each `@ObjectDefinition`. The topmost parent `resourceKey` determines the collection name when inheritance is used.

### Base entity classes

Prefer `AbstractUUIDEntity` (and its helpers) instead of raw `UUIDEntity`. Available variants include:

- `NamedUUIDEntity`: searchable, unique `name` (nameField=true, required).
- `NonUniqueNamedUUIDEntity`: searchable `name`, not unique.
- `ReadOnlyNamedUUIDEntity` / `FixedNamedUUIDEntity`: immutable name (readOnly=true).
- `TaggedAndNamedUUIDEntity`: adds tagging to a named entity.
- `AssignableUUIDEntity`: supports assignment semantics (implements `AssignableDocument`).
- `PersonalUUIDEntity`: marks personal-scope entities.
- `SingletonUUIDEntity`: one-per-tenant singleton base.

If you build a custom subclass with a name field, also implement `NamedDocument` so the framework can read the display name.

Example custom named entity:

```java
@ObjectDefinition(resourceKey = Project.RESOURCE_KEY, type = ObjectType.COLLECTION)
@TableView(defaultColumns = { "name", "enabled" })
public class Project extends AbstractUUIDEntity implements NamedDocument {

    public static final String RESOURCE_KEY = "project";

    @ObjectField(type = FieldType.TEXT, searchable = true, nameField = true)
    @Validator(type = ValidationType.REQUIRED)
    private String name;

    @ObjectField(type = FieldType.BOOL, defaultValue = "true")
    private Boolean enabled;

    @Override
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Boolean getEnabled() { return enabled; }

    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
```

All entities implement `getResourceKey()`; concrete subclasses should return their `RESOURCE_KEY` constant to match the `@ObjectDefinition` resourceKey.

### Defining an entity

```java
@ObjectDefinition(resourceKey = MyEntity.RESOURCE_KEY, type = ObjectType.COLLECTION, scope = ObjectScope.GLOBAL)
@TableView(defaultColumns = { "name", "enabled" })
@GenerateEvents
@UniqueIndex(fields = { "name" })
public class MyEntity extends UUIDDocument {

    public static final String RESOURCE_KEY = "myEntity";

    @ObjectField(type = FieldType.TEXT, searchable = true)
    @Validator(type = ValidationType.REQUIRED)
    private String name;

    @ObjectField(type = FieldType.BOOL, defaultValue = "false")
    private Boolean enabled;

    // getters/setters
}
```

Key points:
- `@ObjectDefinition`: type `COLLECTION`, `SINGLETON`, or `EMBEDDED`; scope `GLOBAL`, `ASSIGNED`, or `PERSONAL`.
- `RESOURCE_KEY`: `public static final String` matching the `resourceKey` in `@ObjectDefinition`.
- Indexing: `@UniqueIndex` for unique compound indexes; `searchable = true` on fields for non-unique index + search dropdown; `@Index` is available but searchable is preferred.
- CRUD pages: COLLECTION entities automatically get search/create/update/delete pages.

### Object references

```java
@ObjectField(type = FieldType.OBJECT_REFERENCE, references = OtherEntity.RESOURCE_KEY)
@Validator(type = ValidationType.OBJECT_TYPE)
private OtherEntity other;
```
- `references` must point to the target entity `RESOURCE_KEY`; database layer auto-loads.
- For lazy loading large targets, store `UUIDReference` instead of the concrete type and manually resolve when used.

### Embedded objects

```java
@ObjectField(type = FieldType.OBJECT_EMBEDDED, references = Address.RESOURCE_KEY)
private Address address;
```
- Parent collection stores the embedded object. Parent must include the embedded field; child still needs its own `@ObjectDefinition` with `type = EMBEDDED`.

## Fields: types, renderers, options, validation

Each field needs `@ObjectField` on a private member with public accessors. I18n entries: `<template>.name`, `<template>.names`, and for each field `<field>.name`/`<field>.desc` in `src/main/resources/i18n/<template>.properties`. Custom views require `<view>.name` in the same bundle.

### Common FieldType usage

- `TEXT`, `TEXT_AREA`, `PASSWORD`, `HTML`, `MARKDOWN_EDITOR`, `HTML_EDITOR` via renderer.
- `BOOL`, `INTEGER`, `LONG`, `DECIMAL`, `DATE`, `TIME`, `TIMESTAMP`.
- `ENUM`: set `validation` to the enum type (RESOURCE_KEY/OBJECT_TYPE as appropriate).
- `OBJECT_REFERENCE`, `OBJECT_EMBEDDED`, `TEMPLATE_REFERENCE` for relationships.
- `ATTACHMENT`, `IMAGE`, `OPTIONS`, `COUNTRY`, `ISO_CURRENCY`.

### Renderers (FieldRenderer)

Select a renderer to control UI input. Examples:
- `DROPDOWN` / `DROPDOWN_MENU`: render select inputs for references.
- `TAGS`: tag input for text collections.
- `OPTIONAL`: hides read-only empty fields.
- `SET_PASSWORD`: password setter UI.
- `HTML_EDITOR`, `MARKDOWN_EDITOR`, `RICH_EDITOR`, `TEXT_EDITOR`, `JAVA_EDITOR`, `CSS_EDITOR` for specialized editors.
- `COLLECTION`: collection selector for text lists.

### FieldOptions

- `SEARCH_REQUIRE_REFERENCE_READ`: restrict search to users with permissions on referenced objects.
- `CASCADE_DELETE`: deleting parent deletes referenced object.
- `CASCADE_ON_DELETED_REFERENCE`: deleting referenced object deletes the parent.
- `SHOW_IN_WIZARD_SUMMARISE`: show hidden fields in wizard summaries.
- `DISABLE_FORM_ENCODING`: disable HTML encoding.
- `SYSTEM_ONLY_VIEW`: visible only in system tenant.
- `AUTOSAVE_VIEW`: auto-save on change.
- `AUTOMATIC_ENCRYPTION`: transparently encrypt/decrypt.
- `MANUAL_ENCRYPTION`: encrypt on save; caller decrypts explicitly.

### Validators

Attach `@Validator` to enforce validation. Common types: `REQUIRED`, `LENGTH`, `REGEX`, `RANGE`, `EMAIL`, `DOMAIN`, `HOSTNAME`, `IPV4`, `IPV6`, `OBJECT_TYPE`, `RESOURCE_KEY`. Multiple validators can coexist.

### Views and layout

- Fields can be limited to specific `FieldView` scopes (READ/CREATE/UPDATE). Excluded scopes hide the field (or remove in READ).
- Use `view` to place fields on tabs/sections; ensure `<view>.name` i18n exists.
- `@TableView` controls list columns, sort, and dynamic columns. Implement `DynamicColumnService` and use `@DynamicColumn` when columns are computed at runtime.

## Services and database access

Create a Spring service per entity that extends an `AbstractUUIDObjectService` implementation. The base classes wire in the right database abstraction and expose lifecycle hooks so your own service methods stay thin and reusable.

### Base service implementations

- `AbstractUUIDObjectServceImpl<T extends UUIDEntity>` injects a `TenantAwareObjectDatabase<T>` as `objectDatabase` and requires `getResourceClass()`. Override lifecycle hooks when needed:
    - `beforeSave(T)` / `afterSave(T)` wrap `saveOrUpdate`.
    - `beforeDelete(T)` wraps `deleteObject` / `deleteObjectByUUID`.
    - `setupDefaults(T)` runs during `createNew(ObjectTemplate)` before returning the new instance.
    - `createIfNotExisting(T)` idempotently inserts seeded data (throws if UUID is blank; no-op if already present).
- `AbstractAssignableUUIDObjectServiceImpl<T extends AssignableDocument>` uses `AssignableObjectDatabase`; supports `beforeSave` / `afterSave`; implements search pagination in-memory via `Stream`.
- `AbstractPersonalUUIDObjectServceImpl<T extends PersonalUUIDEntity>` uses `PersonalObjectDatabase`; validates ownership on save/delete via `validateSave` / `validateDelete`; populates owner when missing in `createIfNotExisting`; provides `deleteAll()` that iterates personal records.

All implementations satisfy `UUIDObjectService`: `getObjectByUUID`, `saveOrUpdate`, `deleteObject`, `deleteObjectByUUID`, `allObjects`, `deleteAll`, `searchTable`, `countTable`, `createNew`, optional `onObjectStashed`, and deprecated `collection`.

### TenantAwareObjectDatabase quick reference

Injected into `AbstractUUIDObjectServceImpl` as `objectDatabase`:
- Fetch: `get(uuid, class)`, `get(class, SearchField...)`, `getOr(class, SearchField...)`, `list`/`stream`.
- Persist: `saveOrUpdate`, `stashObject` (stash in session), `delete(object)`, `delete(uuid, class)`, `delete(class, SearchField...)`, `deleteIfExists`, `deleteAll`.
- Query: `count`, `searchObjects` (optionally with `SortOrder` + `sortField`), `searchTable` (paged and sorted), `table` (search + paging), `searchCount`, `max`/`min`, `sumLongValues`/`sumDoubleValues`.

### SearchField patterns

`SearchField` maps a field’s resource key to a query predicate and feeds `TenantAwareObjectDatabase` methods. Builders:
- Equality/negation: `eq`, `not`, `in`, `all` (match all values).
- Pattern and ranges: `like`, `gt`, `gte`, `lt`, `lte`.
- Logical grouping: `or(...)`, `and(...)` to combine multiple fields.
- Utilities: `add` to append filters; `process(SearchTransformer)` to rewrite values (e.g., tenant scoping); `mark()`/`isMarked()` for downstream tagging; `filter(stream, SearchField...)` for in-memory post-filtering. The primary key can be addressed via `_id` or `UUID`.

### Implementing custom service methods

- Compose a thin method that builds `SearchField` filters and calls the database, e.g., `objectDatabase.get(Resource.class, eq("name", name), not("enabled", false))`.
- For paged UI tables, reuse `searchTable(start, length, sortOrder, sortField, filters...)` and `countTable(filters...)` to keep generated screens in sync.
- Use `createIfNotExisting` for seeding reference data with a fixed UUID. Ensure the object has a non-blank UUID before calling.
- If you need dynamic columns, implement `DynamicColumnService` on the same service and annotate the entity’s `@TableView` with `@DynamicColumn` entries.

## Controllers

CRUD APIs and UI are generated from templates; add controllers only for custom endpoints or flows.

- Discovery: annotate with Spring `@Controller` **and** `org.pf4j.Extension` so the plugin framework registers the bean inside the plugin classloader.
- Base class: extend `AuthenticatedController` to get permission helpers (`assertRead`, `assertWrite`, etc.) and user/system context wiring. `ControllerInterceptor` enforces that `@AuthenticatedContext` may only appear on methods of `AuthenticatedController` subclasses.
- Context annotation: apply `@AuthenticatedContext` to a controller method to declare how the permission context is set before invocation:
    - `user=true`: use the current authenticated principal (session principal).
    - `system=true`: elevate to system context.
    - `preferActive=true`: if a session exists, use it; otherwise fall back to system context (combine with `system=true` when you want this fallback behavior).
    - With no flags, the interceptor sets a user context.
- Lifecycle: `ControllerInterceptor` runs preHandle to set up context, postHandle to clear it, and afterCompletion to map common failures to HTTP responses (403 for `AccessDeniedException`, 401 for `UnauthorizedException`, warn on 404). Keep handlers exception-safe or let `ExceptionHandlingController`/`AuthenticatedController` propagate.

Example controller snippet:

```java
@Controller
@Extension
public class ProjectController extends AuthenticatedController {

    @GetMapping("/api/projects/{uuid}")
    @ResponseBody
    @AuthenticatedContext(user = true)
    public ProjectDTO getProject(@PathVariable String uuid) {
        assertRead(Project.RESOURCE_KEY);
        return mapper.toDto(projectService.getObjectByUUID(uuid));
    }

    @PostMapping("/api/projects/{uuid}/sync")
    @ResponseBody
    @AuthenticatedContext(system = true, preferActive = true)
    public RequestStatus triggerSync(@PathVariable String uuid) {
        projectService.sync(uuid);
        return RequestStatusImpl.success();
    }
}
```

## Extension points

Use PF4J to make plugin behaviours pluggable:

- Define an interface that extends `org.pf4j.ExtensionPoint` (e.g., `MenuProvider extends ExtensionPoint`).
- Implement the interface and annotate each implementation with `@Extension`.
- The plugin runtime registers them as Spring beans; fetch all implementations with `ApplicationServiceImpl.getInstance().getBeans(MyExtension.class)` or the shortcut `App.beans(MyExtension.class)`.
- This pattern powers menus, providers, and other extensible hooks inside plugins.

## Building an authentication mechanism

Authentication now uses provider+UUIDReference wiring (no `AuthenticationModule` persistence). To add a new authenticator:

1) **Provider implementation**
- Implement `AuthenticationProvider` (extends `ExtensionPoint`) and annotate with `@Extension`.
- Return a stable UUID from `getAuthenticatorUUID()` and a short key from `getAuthenticatorKey()` (used to resolve pages and i18n). Supply display text via `getName()` and enrollment/management endpoints via `getEnrollmentUri()` / `getManagementUri()`.
- Implement `hasSufficientCredentials(User)` and `supportsMultipleCredentials()`; optionally override `supportsCredentialReset()`.

2) **Authentication page**
- Create an `AuthenticationPage<?>` subclass for the user-facing flow. Its `getAuthenticatorUUID()` must match the provider UUID. Use the provider key when resolving titles/bodies (i18n keys `<key>.verifyIdentity.title|body`).
- If you need code entry or device selection, follow the pattern in existing pages: pull the provider with `authenticationService.getAuthenticationProviderByUUID(getAuthenticatorUUID())`, gate missing providers, and use provider metadata (key, UUID) for redirects.
- Register the page for the provider during startup: `authenticationService.registerAuthenticationPage(provider, MyAuthPage.class);`. This lets `AuthenticationService.getAuthenticationPage(key)` resolve to your page.

3) **Enrollment/management UX**
- Provider `getEnrollmentUri()` should point to a controller or page that enrolls credentials; `getManagementUri()` should manage existing credentials. Both typically expect a `returnTo` parameter for navigation.
- In manage-credentials flows, the framework renders cards using provider key/UUID and `hasSufficientCredentials`. Ensure your provider returns accurate enrollment/support flags so UI badges are correct.

4) **Policy wiring and references**
- Policies store `Collection<UUIDReference>` for required/optional authenticators. Construct references with the provider UUID/name or fetch them via `authenticationService.getAuthenticationModuleByUUID(uuid)` / `getAuthenticationModuleByResourceKey(key)` (these return `UUIDReference`).
- `AuthenticationState` tracks required/optional selections by `UUIDReference`; pages should avoid using legacy `AuthenticationModule` lookups.

5) **Optional/temporary flows**
- Optional authenticator selection uses provider UUIDs in cookies/forms. If you emit redirects or defaults, store the provider UUID (not the resource key).
- Temporary flows use `authenticationService.launchTemporaryAuthentication(name, redirectURI, UUIDReference...)`; pass provider references when starting SMS/email/phone verification, etc.

6) **Internationalization**
- Provide i18n entries under your authenticator key: `<key>.verifyIdentity.title`, `<key>.verifyIdentity.body`, and any validation/enrollment text your pages render. Enrollment dropdowns and cards use these keys.

## Plugin POMs and wiring

Most development happens in PF4J plugins (one Maven module per plugin). Use `jadaptive-common/jadaptive-sample-plugin/pom.xml` as the template and adjust IDs and metadata.

- Plugin metadata lives in `<properties>`: `plugin.id`, `plugin.class` (must extend `AbstractSpringPlugin` and live in the module’s base package), `plugin.version`, `plugin.provider`, `plugin.projectUrl`, `plugin.dependencies` (comma-separated plugin IDs). The plugin class’s package root and its subpackages are what Spring scans; keep components there.
- Inter-plugin dependencies: add the other plugin as `<dependency scope="provided">` and list its plugin ID in `<plugin.dependencies>`. Only the first entry in `plugin.dependencies` is eligible for `@Autowired` injection; for services from additional plugins, use `@AutowiredExtension`.
- Classloading: dependencies declared in the plugin POM are private to that plugin. Exclude shared libs such as SLF4J or Jackson if they arrive transitively to avoid clashes with the host runtime.
- Build plugins with the PF4J plugin generator (`pf4j-plugin-generator` in the build) and ensure the plugin class is declared in `plugin.properties` via the POM properties above.

Examples

- Excluding host-provided libs from a provided plugin dependency:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>foo-plugin</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </exclusion>
        <exclusion>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

- Autowiring across plugins:

```java
@Extension
public class ReportingController extends AuthenticatedController {

        @Autowired // allowed for the first plugin listed in plugin.dependencies
        private PrimaryPluginService primary;

        @AutowiredExtension // use for services from additional plugins declared in plugin.dependencies
        private SecondaryPluginService secondary;
}
```

- Declaring dependent plugins (order matters for `@Autowired`):

```xml
<properties>
    <plugin.dependencies>primary-plugin,secondary-plugin,analytics-plugin</plugin.dependencies>
</properties>
```
Only `primary-plugin` is eligible for direct `@Autowired`; `secondary-plugin` and `analytics-plugin` must be accessed with `@AutowiredExtension`.

## Events

`EventService` provides publish/subscribe for entity lifecycle and system events.

- Register listeners (typically in `StartupAware.onApplicationStartup`) using:
    - `created/updated/deleted/assigned/unassigned/changed/saved/committed(Class<T>, handler)` for entity lifecycle.
    - Pre-hooks `creating/updating/deleting/saving(Class<T>, handler)` to run before persistence.
    - `on(Class<? extends SystemEvent>, handler)` for custom events.
    - `on(String resourceKey, listener)` or `on(listener, resourceKeys...)` to bind directly by resource or event key (see `Events.created(...)`, etc.).
- Emit events with `publishEvent(SystemEvent)`. Wrap domain data in a `SystemEvent` subclass or use `ObjectEvent`/`ObjectUpdateEvent` for entity payloads.
- Listener ordering respects `EventListener.weight()` (lower first). Async events (`event.async()==true`) run on a cached thread pool with tenant/user context propagated; sync handlers may throw back to the caller.
- Use `haltEvents()` / `resumeEvents()` to suppress dispatch on the current thread (e.g., during bulk imports). `eventRegistrations(Runnable)` + `executePreRegistrations()` queue registration until the bus is ready.

Examples

```java
// Refresh SSHD when configuration changes
eventService.updated(SSHDConfiguration.class, evt -> sshdRefresher.apply(evt.getObject()));

// Listen to cluster node connectivity
eventService.on(ClusterNodeConnectedEvent.class, evt -> clusterBroadcaster.notifyChange());
eventService.on(ClusterNodeDisconnectedEvent.class, evt -> clusterBroadcaster.notifyChange());

// Custom feature toggle event
public class FeatureToggledEvent extends SystemEvent { }

eventService.on(FeatureToggledEvent.class, evt -> featureCache.invalidate());
eventService.publishEvent(new FeatureToggledEvent());
```

## Table actions and filters

- Add row actions with `@TableAction` on entities (icon, permission, URL, target). Use `filter = MyFilter.class` where `MyFilter implements ActionFilter` to decide if the action is shown based on the serialized `AbstractObject` (e.g., `object.get("draft")`).

## Custom pages with HtmlPage

Create custom UI pages when autogenerated screens are insufficient.

```java
@Component
@RequestPage("plans/{uuid}")
@PageDependencies(extensions = { "bootstrap", "fontawesome", "jadaptive-utils", "jadaptive-forms" })
@PageProcessors(extensions = { "i18n", "help" })
public class ApprovePlanPage extends AuthenticatedPage {

    private String uuid; // bound from {uuid}

    @Override
    public String getUri() { return "approve-plan"; }

    @Override
    public void generateContent(Document document) throws IOException {
        // manipulate DOM from ApprovePlanPage.html
    }
}
```

Rules:
- Extend `HtmlPage` or `AuthenticatedPage` (requires login). Mark as `@Component` with a unique URI served at `/app/ui/<uri>`.
- Resources live at `src/main/resources/<package path>/`: `SimpleName.html`, `SimpleName.css`, `SimpleName.js`, optional `SimpleNameHelp.html`. No inline CSS or `style` attributes—use the CSS file.
- `@RequestPage("path/{uuid}")` binds `{uuid}` to a private field named `uuid` when the page loads; do not fetch path variables from `Request` manually.
- For POST handling on pages, implement `FormProcessor<MyForm>` and declare an interface `MyForm` with getters for form fields. The framework instantiates and populates it, then calls `processForm(Document, MyForm)` on POST to the same URI. No need for @Override annotation on processForm as its found via reflection.
- For request-scoped state that must persist across POSTs (queues, wizard progress), use the servlet session obtained via `Request.get().getSession()`; the `Session` helper does not store data in `HttpSession`.
- Internationalize visible strings in page HTML by moving them into the appropriate bundle (e.g., `src/main/resources/i18n/<bundle>.properties`) and annotating elements with `jad:bundle="<bundle>"` and `jad:i18n="<key>"`. Avoid hard-coded text in templates; the `i18n` processor will replace these attributes at render time.
- Parameterize i18n strings with standard Java MessageFormat placeholders (`{0}`, `{1}`, ...); supply values in HTML via `jad:attr0`, `jad:attr1`, etc., alongside `jad:bundle`/`jad:i18n`.
- `@PageDependencies` lists required `PageExtension` names (e.g., `bootstrap`, `fontawesome`, `jadaptive-utils`, `jadaptive-forms`; `jquery` exists but prefer vanilla JS).
- `@PageProcessors`: post-generation processors (e.g., `i18n` to resolve `jad:bundle` / `jad:i18n`; `help` to wire `SimpleNameHelp.html`).
- Lifecycle: `beforeProcess` → dependencies → extenders `processStart` → `generateContent` → extenders `generateContent` → feedback injection → scripts/styles/processors → `documentComplete` → extenders `processEnd` → `afterProcess`. Override `isCacheable`/`getMaxAge` to adjust caching; `onCreated` for init; `processPost` or `FormProcessor` for POST handling.
- Extenders: any `HtmlPageExtender` with `isExtending` true runs `processStart`, `generateContent`, `processEnd` around the page.

## Internationalization checklist

- Template names: `<template>.name`, `<template>.names`.
- Field labels/descriptions: `<field>.name`, `<field>.desc`.
- Custom views/tabs: `<view>.name`.
- Page help: `SimpleNameHelp.html` plus i18n strings referenced via `jad:i18n`/`jad:bundle` processed by the `i18n` processor.

## Practical patterns

- Use `searchable=true` for fields you want indexed and available in search UI.
- For references, set `references` to the target `RESOURCE_KEY`; switch to `UUIDReference` for lazy loading.
- Avoid inline styling; keep HTML clean and rely on CSS/JS resources resolved automatically.
- Use validators generously to prevent bad data and to surface UI errors early.
- Add `@GenerateEvents` when audit trails are important.
