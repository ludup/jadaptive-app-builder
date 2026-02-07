# Agent quick-start for Jadaptive entities

This framework stores entities in MongoDB with annotated POJOs that drive validation, storage, and auto-generated UI. Follow these rules when adding or modifying entities, fields, and views.

## Core entity annotations

- `@ObjectDefinition`: Defines an entity. Types: COLLECTION (default, multi), SINGLETON (one per tenant), EMBEDDED (inline, not stored separately). Scopes: GLOBAL (default), ASSIGNED (can be linked to users/roles), PERSONAL (owned per user). `resourceKey` is the template/collection name; inherited classes can override but the topmost parent’s key determines the actual collection.
- `@UniqueIndex` (class-level): Adds a Mongo unique index. Use for compound uniqueness. For non-unique indexes prefer `searchable=true` on fields; alternatively use `@Index`.
- `@TableView`: Controls the generated search/listing page for COLLECTION entities (default columns, dynamic columns, sort, multi-delete, parent/child column inclusion). CRUD pages (search/create/update/delete) are auto-provisioned for COLLECTION objects.
- `@GenerateEvents`: Auto-generates event objects for auditing.

## Fields

- Declare private fields with `@ObjectField`; supply public getters/setters. `resourceKey` defaults to the field name.
- `type` defaults to AUTO (derived from Java type). Override for specific rendering/validation (e.g., `OBJECT_REFERENCE`, `OBJECT_EMBEDDED`, `ENUM`, `PASSWORD`, `HTML`, etc.).
- `searchable=true` adds a non-unique index and surfaces the field in the search dropdown.
- `FieldRenderer` overrides how the UI renders inputs (see `FieldRenderer` enum for options such as DROPDOWN, DROPDOWN_MENU, TAGS, OPTIONAL, HTML_EDITOR, MARKDOWN_EDITOR, SET_PASSWORD, etc.).
- `FieldOptions` adjust behavior (from `FieldOptions` enum):
  - `SEARCH_REQUIRE_REFERENCE_READ`: Enforce permission when searching by references.
  - `CASCADE_DELETE` / `CASCADE_ON_DELETED_REFERENCE`: Cascade deletions in either direction for reference fields.
  - `SHOW_IN_WIZARD_SUMMARISE`: Show hidden fields in wizard summary.
  - `DISABLE_FORM_ENCODING`: Disable HTML encoding in forms.
  - `SYSTEM_ONLY_VIEW`: Only show to system-tenant users.
  - `AUTOSAVE_VIEW`: Autosave when the field changes.
  - `AUTOMATIC_ENCRYPTION`: Transparent encrypt/decrypt at rest.
  - `MANUAL_ENCRYPTION`: Encrypt on save; manual decrypt on read.
- `FieldType` may imply allowed `ValidationType` (see `FieldType` enum `getOptions()`): e.g., `TEXT` supports LENGTH/REGEX; `LONG/INTEGER/DECIMAL` support RANGE; `OBJECT_REFERENCE/OBJECT_EMBEDDED` expect OBJECT_TYPE/RESOURCE_KEY; `ENUM` expects OBJECT_TYPE.
- `@Validator` on fields enforces validation (multiple allowed). Common: IPV4/IPV6/HOSTNAME, RANGE, LENGTH, REGEX, OBJECT_TYPE, RESOURCE_KEY, DOMAIN, EMAIL.
- `view` on fields places them into tabs/sections; add `<view>.name` in `src/main/resources/i18n/<template>.properties` when using custom views.
- For each field add i18n entries `<field>.name` and `<field>.desc` in `src/main/resources/i18n/<template>.properties`.

## Templates and i18n

- Every template needs `<template>.name` (singular) and `<template>.names` (plural) i18n entries in `src/main/resources/i18n/<template>.properties`.
- Field i18n: `<field>.name` and `<field>.desc` as above.
- Additional i18n entries are required for custom views/tabs (`<view>.name`).

## Rendering rules (AbstractObjectRenderer highlights)

- Forms and tables are generated from `ObjectTemplate` and `TemplateView` definitions. If no views exist, a single dynamic view is created using all fields.
- `FieldView` (READ/CREATE/UPDATE) controls visibility; if a field’s views exclude the current scope, it is hidden (or removed for read-only display). `FieldRenderer.OPTIONAL` hides read-only empty fields.
- Collection fields: OBJECT_EMBEDDED collections render via `TableRenderer`; reference collections use `CollectionSearchFormInput`; ENUM collections use `MultipleSelectionFormInput`; TEXT with TAGS or COLLECTION render tag/collection inputs; ATTACHMENT uses multiple attachment input; OPTIONS uses options input against a referenced template.
- Reference selectors choose renderer based on `FieldRenderer` (DROPDOWN/DROPDOWN_MENU vs search modal). Meta `decorate=false` disables form adornments; meta `url` can override the search endpoint.
- Required fields set `required` on inputs; read-only fields set `readonly`. Hidden flags and `FieldOptions.SYSTEM_ONLY_VIEW` gate visibility.

## Services, database access, and dynamic columns

- Create a Spring service per entity by extending an `AbstractUUIDObjectService` variant. The common base `AbstractUUIDObjectServceImpl` injects `TenantAwareObjectDatabase` as `objectDatabase`; override `getResourceClass()` and add custom methods that compose `SearchField` constraints and call `objectDatabase` queries.
- Override hooks when you need lifecycle logic: `beforeSave` and `afterSave` wrap `saveOrUpdate`; `beforeDelete` wraps `deleteObject`/`deleteObjectByUUID`; `setupDefaults` runs during `createNew`; `createIfNotExisting` is available to idempotently insert pre-seeded records (throws if UUID is blank).
- `TenantAwareObjectDatabase` helpers: `list`/`stream`, `get`/`getOr`, `saveOrUpdate`, `delete`/`deleteIfExists`, `count`, `searchObjects`/`searchTable`/`table`, `searchCount`, `sumLongValues`/`sumDoubleValues`, `max`/`min`, `stashObject`, `deleteAll`. Pass `SearchField` varargs to filter, or `SortOrder` + `sortField` where supported.
- `SearchField` builders: `eq`, `not`, `like`, `in`, `all`, `gt`, `gte`, `lt`, `lte`, plus logical `or(...)` / `and(...)`; use `_id` (or `UUID`) for the primary key. `SearchField.add(...)` appends filters; `.process` lets a `SearchTransformer` rewrite values; `.mark` tags fields for downstream logic. Use collections helper `SearchField.filter(stream, filters)` for in-memory filtering.
- Use the extended service bases when they match your entity type: `AbstractAssignableUUIDObjectServiceImpl` (assignable objects via `AssignableObjectDatabase`), `AbstractPersonalUUIDObjectServceImpl` (per-user `PersonalUUIDEntity` with owner validation and personal database), and `AbstractUUIDObjectServceImpl` (default UUID entities). These wire the correct database abstraction and life-cycle hooks.
- Implement `DynamicColumnService` when dynamic columns are needed, and add `@DynamicColumn` to `@TableView` to surface those columns.

## Controllers

- Normal CRUD JSON/UI is generated automatically from templates. Only add controllers for custom APIs or flows.
- Annotate custom controllers with Spring `@Controller` **and** `org.pf4j.Extension` so the plugin framework discovers and wires them.
- Extend `AuthenticatedController` to gain permission helpers and automatic context management. Without it, `@AuthenticatedContext` is rejected by `ControllerInterceptor`.
- Use `@AuthenticatedContext` on controller methods to declare execution context:
  - `user=true`: enforce a user context based on the current authenticated principal.
  - `system=true`: run with system context (elevated); combine with `preferActive=true` to fall back to system only when no session exists.
  - If no flags, defaults to user context.
- The interceptor sets up context before invocation and clears it afterward; unauthorized/system misuse raises `AccessDeniedException` or `IllegalArgumentException`.

Example minimal controller:

```java
@Controller
@Extension
public class ProjectApi extends AuthenticatedController {

  @GetMapping("/api/projects/{uuid}")
  @ResponseBody
  @AuthenticatedContext(user = true) // ensure user context
  public ProjectDTO getProject(@PathVariable String uuid) {
    assertRead(Project.RESOURCE_KEY);
    return mapper.toDto(projectService.getObjectByUUID(uuid));
  }

  @PostMapping("/api/projects/{uuid}/system-refresh")
  @ResponseBody
  @AuthenticatedContext(system = true, preferActive = true) // prefer session user; fall back to system
  public RequestStatus refresh(@PathVariable String uuid) {
    projectService.refresh(uuid);
    return RequestStatusImpl.success();
  }
}
```

## Extension points (@Extension)

- To plug behaviour, declare an interface that extends `org.pf4j.ExtensionPoint` and annotate implementations with `@Extension`.
- The plugin runtime registers these as Spring beans; retrieve all implementations via `ApplicationServiceImpl.getInstance().getBeans(MyExtension.class)` or `App.beans(MyExtension.class)`.
- This is the preferred mechanism for pluggable behaviours (menus, providers, handlers) inside plugins.

## Authentication provider quick path

- Authentication is provider-based: implement `AuthenticationProvider` (PF4J `ExtensionPoint`) and expose a stable `getAuthenticatorUUID()` plus a short `getAuthenticatorKey()` string. `getName()` supplies the display label; enrollment/management URIs come from `getEnrollmentUri()` / `getManagementUri()`.
- Add `@AuthenticationConfig(resourceKey=..., icon=..., iconGroup=...)` to any tenant level singleton configuration object used by the authenticator so it appears on the Authentication Policies page for view/edit. Ensure the class’s i18n bundle includes `<resourceKey>.name` and `<resourceKey>.desc` entries.
- If you use a system-level configuration singleton instead, annotate it with `@ConfigurationItem(resourceKey=..., icon=..., iconGroup=..., bundle=..., system=true)` so it shows on the System Configuration pages. Provide `<resourceKey>.name` and `<resourceKey>.desc` in that bundle. Use either `@AuthenticationConfig` or `@ConfigurationItem`, not both on the same class.
- Register the authenticator and its main `AuthenticationPage` with `AuthenticationService.registerAuthenticationPage(provider, Page.class)` during startup (e.g., from a `StartupAware` bean) so policies can route to the page by provider key.
- Policies store `UUIDReference` objects, not `AuthenticationModule` entities. Anywhere you need an authenticator reference, use `new UUIDReference(provider.getAuthenticatorUUID(), provider.getName())` or retrieve existing references from `AuthenticationService` helper methods.
- Each authenticator must have at least one `AuthenticationPage` (extends `AuthenticationPage<?>`) that returns the same UUID from `getAuthenticatorUUID()` and uses the provider key to pick the page via `AuthenticationService.getAuthenticationPage(key)`. Register the page against the provider by calling `authenticationService.registerAuthenticationPage(provider, MyPage.class)` (often done from a `StartupAware` or similar extension).
- Each authenticator must have at least one `AuthenticationPage` (extends `AuthenticationPage<?>`) that returns the same UUID from `getAuthenticatorUUID()` and uses the provider key to pick the page via `AuthenticationService.getAuthenticationPage(key)`. Register the page against the provider by calling `authenticationService.registerAuthenticationPage(provider, MyPage.class)` (often done from a `StartupAware` or similar extension). Inside authentication pages, prefer `AuthenticationState.getUser()` to access the authenticating user—`sessionUtils.getCurrentUser()` may be empty because the session is not yet established.
- Optional and temporary flows rely on provider UUIDs: `AuthenticationState` tracks `UUIDReference` for required/optional steps, and `AuthenticationService.launchTemporaryAuthentication(name, redirectURI, UUIDReference...)` expects provider references.
- For enrollment/management UIs, keep the provider UUID stable and bundle i18n at `<authenticatorKey>.verifyIdentity.*` for cards/buttons; optional pages expect these keys for titles/bodies.

## Table actions with filters

- Use `@TableAction` to surface row actions in search results. You can gate visibility via an `ActionFilter` implementation that inspects the serialized `AbstractObject` (e.g., check `object.get("draft")`). Example: show an "authorize" action only when `draft == true`.

## Events

- `EventService` lets you listen to entity lifecycle and custom system events. Register handlers early (e.g., `StartupAware.onApplicationStartup`).
- Common hooks: `created`, `updated`, `deleted`, `assigned`, `unassigned`, `changed` (create/update/delete), `saved` (create+update), pre-hooks `creating`, `updating`, `deleting`, `saving`, and `committed` (post-update/delete). Use `eventService.on(Class<? extends SystemEvent>, listener)` for non-entity events.
- `publishEvent(SystemEvent)` emits custom events; wrap details in a `SystemEvent` subclass (or `ObjectEvent`/`ObjectUpdateEvent` when carrying entities).
- Listeners can be keyed by resource key via `on(String resourceKey, listener)` or by event key (`Events.created(...)`, etc.). Listener order is by `EventListener.weight()`.
- `haltEvents()` / `resumeEvents()` toggle dispatch per thread; `eventRegistrations(Runnable)` + `executePreRegistrations()` defer registration until the event bus is ready.
- Async events run in the background and preserve tenant/user context when possible; non-async can throw back to caller.

Examples:

```java
// React to config updates
eventService.updated(MyConfig.class, evt -> configRefresher.refresh(evt.getObject()));

// React to any change on an entity
eventService.changed(Project.class, evt -> audit.log("project-change", evt.getObject().getUuid()));

// Custom system event
public class FeatureToggledEvent extends SystemEvent { }

eventService.on(FeatureToggledEvent.class, evt -> cache.invalidate("features"));
eventService.publishEvent(new FeatureToggledEvent());
```

## Plugin POMs (quick rules)

- Start from `jadaptive-common/jadaptive-sample-plugin` POM; it shows the standard config and can be copied/adjusted for new plugins.
- Each Maven module is typically a PF4J plugin. Declare plugin metadata in `<properties>`: `plugin.id`, `plugin.class` (extends `AbstractSpringPlugin` in the base package), `plugin.version`, `plugin.provider`, `plugin.projectUrl`, `plugin.dependencies`.
- If your plugin depends on other plugins, list them as `<dependency scope="provided">` and also in the `<plugin.dependencies>` property. Only the first entry in `plugin.dependencies` can be injected with `@Autowired`; for additional plugin services use `@AutowiredExtension`.
- Dependencies are isolated per plugin classloader. Exclude common transitive libs like SLF4J and Jackson if pulled by your dependencies to avoid conflicts with the host. Jackson is already supplied by the platform (via `jadaptive-api`); do not add explicit `com.fasterxml.jackson.*` dependencies unless absolutely required for a newer module, and even then scope them as `provided` and exclude `jackson-databind`/core from transitives.
- Keep your plugin classes under the base package of the plugin class (the `plugin.class` package); only that package and its children are visible to Spring.

Examples:

- Exclude shared libs from a provided dependency to avoid clashes:

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

- Autowiring across plugins: first `plugin.dependencies` entry can use `@Autowired`; use `@AutowiredExtension` for additional plugins.

```java
@Extension
public class ReportTasks extends AuthenticatedController {

    @Autowired // first plugin listed in plugin.dependencies
    private PrimaryApi primaryApi;

    @AutowiredExtension // services from other declared plugins
    private SecondaryApi secondaryApi;
}
```

## Checklist for new entities

1. Create POJO with `@ObjectDefinition` (type, scope, resourceKey). Add `@UniqueIndex` or `searchable=true` where needed.
2. Add `@TableView` for collection listings (default columns, sort, permissions).
3. Annotate private fields with `@ObjectField`, `FieldType`, `FieldRenderer`/`FieldOptions` as required, and `@Validator` for validation.
4. Add getters/setters.
5. Provide i18n entries: template `name/names`, each field `name/desc`, any custom view names.
6. Add a service (AbstractUUIDObjectService-derived) and optional DynamicColumn support.
7. Consider `@GenerateEvents` if auditing is desired.
8. For embedded objects, ensure parent templates include fields with `FieldType.OBJECT_EMBEDDED` pointing to the child template’s `resourceKey`.

## Additional rules

- Every entity class must declare `public static final String RESOURCE_KEY` matching the resource key used in `@ObjectDefinition`.
- For `FieldType.OBJECT_REFERENCE`, set the `references` attribute of `@ObjectField` to the referenced entity’s `RESOURCE_KEY`. The database layer will auto-load references. If the referenced entity is large/complex and you prefer lazy loading, use `UUIDReference` as the field type; fetch the full object manually when needed.
- Prefer `AbstractUUIDEntity` (and its variants) over raw `UUIDEntity`. Common base variants: `NamedUUIDEntity` (unique, searchable name field), `NonUniqueNamedUUIDEntity` (searchable name, non-unique), `ReadOnlyNamedUUIDEntity` / `FixedNamedUUIDEntity` (immutable name), `TaggedAndNamedUUIDEntity`, `AssignableUUIDEntity`, `PersonalUUIDEntity`, `SingletonUUIDEntity`. If you implement a custom subclass with a name field, also implement `NamedDocument` to supply the name.
- Each entity implements `getResourceKey()`; concrete subclasses must return their `RESOURCE_KEY` constant.

## Custom pages (HtmlPage)

- Create custom pages by extending `HtmlPage` (or `AuthenticatedPage` when login is required). Annotate as a Spring `@Component` with a unique URI; it will be served under `/app/ui/<uri>`.
- Resource mapping: place `SimpleName.html`, `SimpleName.css`, `SimpleName.js` (and optional `SimpleNameHelp.html`) in `src/main/resources/<package path>/`. Inline styles/`style` attributes are not allowed; use the CSS file. When files match the page class name and package, the framework auto-loads them—do not override `getHtmlResource`/`getCssResource`/`getJsResource` unless you intentionally deviate from the default location.
- Page dependencies: most standard pages must include the `bootstrap` extension because the UI is bootstrap-based. Only include `jquery` if the JS actually uses it; prefer vanilla JS per current guidance.
- Lifecycle hooks: override `beforeProcess` / `afterProcess` for pre/post processing; `onCreated` for initialization; `documentComplete` runs after content/extensions are applied. `isCacheable`/`getMaxAge` control cache headers.
- Content flow (GET): load HTML resource → process page dependencies → extenders `processStart` → `generateContent` (page-specific) → extenders `generateContent` → inject feedback → process page extensions (scripts/styles/processors) → `documentComplete` → extenders `processEnd` → `afterProcess`.
- POST handling: if implementing `FormProcessor`, `doPost` proxies request params into the form interface via (`processForm`). Otherwise override `processPost`.
- `@RequestPage("path/{uuid}")`: binds path variables to private fields with matching names (e.g., `private String uuid;`).
- To handle POST on a page, implement `FormProcessor<MyForm>` and declare a nested interface `MyForm` matching form fields; the framework instantiates and populates it, calling `processForm(Document, MyForm)` on POST to the same URI. No need for @Override annotation on processForm as its found via reflection.
- When you need per-user state across page posts, store it in the HTTP session via `Request.get().getSession()`; the `Session` helper is not backed by `HttpSession` and cannot hold per-request data.
- Internationalize all visible text in page HTML: move literals into an i18n bundle (e.g., `src/main/resources/i18n/aiContext.properties`) and reference them with `jad:bundle="<bundleName>"` and `jad:i18n="<key>"` on the element that renders the text.
- Parameterize i18n strings with Java MessageFormat placeholders (`{0}`, `{1}`, ...); provide values via `jad:attr0`, `jad:attr1`, etc., alongside `jad:bundle`/`jad:i18n` in the HTML.
- `@PageDependencies(extensions = {"bootstrap", "fontawesome", "jadaptive-utils", "jadaptive-forms", ...})` pulls required PageExtension names (strings match extension `getName()`). `jquery` exists but modern pages should prefer vanilla JS.
- `@PageProcessors(extensions = {"i18n", "help", ...})`: run processors after generation. `i18n` resolves `jad:bundle` / `jad:i18n` attributes into localized text. `help` wires `SimpleNameHelp.html` into the help system.
- Extenders: Any `HtmlPageExtender` with `isExtending` true runs `processStart`, `generateContent`, `processEnd` around your page.
