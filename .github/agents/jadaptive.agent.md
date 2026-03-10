# Agent quick-start for Jadaptive entities

This framework stores entities in MongoDB with annotated POJOs that drive validation, storage, and auto-generated UI. Follow these rules when adding or modifying entities, fields, and views.

## Product implementation (one per app)

- Purpose: `Product` identifies the running product for metadata, licensing flags, branding, and PAYG toggles. Only one `Product` bean should exist at runtime; `ProductServiceImpl` pulls the first `Product` bean from the PF4J/Spring context and falls back to a default no-op product if none is registered ([ProductServiceImpl](jadaptive-app-builder/jadaptive-boot/src/main/java/com/jadaptive/app/product/ProductServiceImpl.java#L20-L113)).
- Defaults: see [Product defaults](jadaptive-app-builder/jadaptive-api/src/main/java/com/jadaptive/api/product/Product.java#L8-L60). Override anything that differs from the generic "Jadaptive App Builder" defaults. Key flags: `requiresRegistration()` defaults true; `isTenantLicensing()` false; `isUserLicensing()` true; `isRevenueGenerating()` true; `supportsPAYG()` false; `getProductId()` defaults to `ProductId.FRAMEWORK`.
- Mandatory overrides for a real product: `getName()`, `getProductCode()` (short stable code), `getProductId()` (pick from `ProductService.ProductId`—add a new enum entry there for new products), and usually `getVendor()` / `getPoweredBy()` / `getVersion()` (often `ApplicationVersion.getVersion()`). Keep `getProductCode()` and `ProductId` unique to the product.
- Registration: annotate the implementation with `@Extension` (PF4J) in the plugin module so the host sees it. There must be exactly one `Product` implementation loaded; remove/disable extras to avoid ambiguous branding.
- Branding assets: `getLogoResource()` and `getFaviconResource()` return classpath resources for PNG/SVG/ICO. `ProductServiceImpl.getLogoResource()` first looks for a `ProductLogoSource` bean and base64-encodes its result; otherwise it uses `ApplicationProperties` overrides `app.logo` / `app.favicon` before falling back to the product defaults ([logo resolution](jadaptive-app-builder/jadaptive-boot/src/main/java/com/jadaptive/app/product/ProductServiceImpl.java#L42-L59)). Implement `ProductLogoSource#getProductLogo()` to return the logo path when you want custom logos without changing `ApplicationProperties`.
- Configuration overrides: operators can override `getName()`, `getPoweredBy()`, `getProductCode()`, `getVendor()`, `getLogoResource()`, `getFaviconResource()` via `ApplicationProperties` keys `app.name`, `app.power`, `app.code`, `app.vendor`, `app.logo`, `app.favicon` ([property lookup](jadaptive-app-builder/jadaptive-boot/src/main/java/com/jadaptive/app/product/ProductServiceImpl.java#L62-L98)). Ensure your defaults are sensible when these keys are absent.
- Licensing flags: set `isTenantLicensing()` true for multi-tenant licensed deployments; set `isUserLicensing()` (defaults true) to indicate per-user licensing applies; set `isRevenueGenerating()` false for non-commercial/bundled products (e.g., free companion apps); set `supportsPAYG()` true only when PAYG billing is available for the product.
- Example minimal implementation:

```java
@Extension
public class MyProduct implements Product, ProductLogoSource {
  public String getName() { return "My Product"; }
  public String getProductCode() { return "MYPROD"; }
  public ProductId getProductId() { return ProductId.SSH_PROXY_ONPREM; }
  public String getVendor() { return "Acme Ltd"; }
  public String getPoweredBy() { return "Powered by Acme"; }
  public String getLogoResource() { return "/app/content/images/myprod-logo.png"; }
  public String getFaviconResource() { return "/app/content/images/myprod-favicon.png"; }
  public String getProductLogo() { return getLogoResource(); }
}
```

## Core entity annotations

- `@ObjectDefinition`: Defines an entity. Types: COLLECTION (default, multi), SINGLETON (one per tenant), EMBEDDED (inline, not stored separately). Scopes: GLOBAL (default), ASSIGNED (can be linked to users/roles), PERSONAL (owned per user). `resourceKey` is the template/collection name; inherited classes can override but the topmost parent’s key determines the actual collection.
- `@UniqueIndex` (class-level): Adds a Mongo unique index. Use for compound uniqueness. For non-unique indexes prefer `searchable=true` on fields; alternatively use `@Index`.
- `@TableView`: Controls the generated search/listing page for COLLECTION entities (default columns, dynamic columns, sort, multi-delete, parent/child column inclusion). CRUD pages (search/create/update/delete) are auto-provisioned for COLLECTION objects.
- `@GenerateEventTemplates`: Auto-generates event objects for auditing.
- `@TableAction`: Surface row actions in search results; can be gated by an `ActionFilter` that inspects the serialized object. Actions can be Target.ROW for row-level or Target.TABLE for table level actions.

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

## Spring Autowiring

The plugin system allows for flexible dependency management. In order for a plugin to utilise components and services in another plugin it must declare a 'provided' scope dependency on that plugin in its `pom.xml` and add the plugin id to the <plugin.dependencies> section of the `pom.xml` <properties>. 

Once a plugin dependency has been added, the PF4J/Spring context will be aware of the relationship and allow for Spring's `@Autowired` annotation to resolve dependencies across plugins. However, due to limitations of the Spring context
architecture, you can only use `@Autowired` on the FIRST plugin in the list of 
<plugin.dependencies>. For example, with the configuration <plugin.dependencies>plugin-a,plugin-b,plugin-c</plugin.dependencies> declared in plugin-z only plugin-a can be autowired into plugin-z. To autowire components and services fro plugin-b or plugin-c, you MUST use the `@AutowiredExtension` annotation instead of `@Autowired`. This annotation is designed to work around the Spring context limitations and allows you to inject dependencies from any plugin in the list of <plugin.dependencies>.

## Dependencies

The plugin system allows for sandboxed dependency management, with dependencies declared in each plugin's `pom.xml` and isolated from the host and other plugins. However, there are some exceptions, any dependencies of jadaptive-boot and jadaptive-api are effectively shared across all plugins and the host application. This means that if you add a dependency to jadaptive-boot or jadaptive-api, it will be available to all plugins without needing to declare it in their `pom.xml`. 

When adding dependencies to your plugin, you should generally prefer to declare them in your plugin's `pom.xml`. If you need to use a dependency that is already included in jadaptive-boot or jadaptive-api, you can simply use it in your plugin without declaring it again.

You need to be careful with transitive dependencies, if you add a dependency to your plugin that has its own dependencies, those transitive dependencies will also be included in your plugin's classpath. This can lead to conflicts if different plugins within your plugins dependencies and/or jadpative-api/jadaptive-boot include different versions of the same transitive dependency. 

If a dependency exists in both the transitive dependencies of your plugin and jadaptive-boot/jadaptive-api, you MUST use exclusions and use the version declared in jadaptive-boot/jadaptive-api to ensure that there is only one version of the dependency in the classpath.

Common examples where problems can arise include logging frameworks (e.g., Log4j, SLF4J) and JSON libraries (e.g., Jackson). If you find that you need to use a different version of a common library than the one included in jadaptive-boot/jadaptive-api, you should first check if the version in jadaptive-boot/jadaptive-api is compatible with your plugin. 

## Templates and i18n

- Every template needs `<template>.name` (singular) and `<template>.names` (plural) i18n entries in `src/main/resources/i18n/<template>.properties`.
- Field i18n: `<field>.name` and `<field>.desc` as above.
- Additional i18n entries are required for custom views/tabs (`<view>.name`).

## Seeded email templates (messages)

- Location and versioning: add a JSON seed in `src/main/resources/system/shared/objects` named `<key>_<version>.json` (e.g., `email_0.0.1.json`). Files run in ascending version order; the TemplateVersionService records the version per key so later files can add new messages without rerunning prior versions.
- Entity shape: each JSON object maps to `com.jadaptive.plugins.email.Message` (see AGENT basics). Set `resourceKey` (e.g., `emailMessages`), `group` (shared grouping label), `name` (unique message name), `enabled`, `archive`, `system` (usually true for seeded messages), and a stable `uuid` meaningful to the context.
- UUID constants: if the plugin has a `MessageTemplates` interface in its base package, add a `public static final String` for the message UUID; if absent, create the interface and add the constant (e.g., `String TICKET_REJECTED = "ticketService.rejected";`).
- Content: add a single `content` entry for locale `DEFAULT` with `MessageContent` shape, a new random content `uuid`, a subject, `htmlTemplate` pointing to the default template, `htmlText` pointing to the HTML resource (`resource://<file>.html`), `enabled`, and `system`.
- HTML resource: place the HTML body in `src/main/resources/defaultMessages/<file>.html`. Reference it from `htmlText` as `resource://<file>.html`.
- Execution: seeds are picked up automatically on startup; TemplateVersionService creates/updates the database entries according to the versioned key.
- Example: CRM seed `01conversationMessages_0.0.2.json` defines message UUID `ticketService.rejected` with `htmlText` `resource://rejected.html` backed by `src/main/resources/defaultMessages/rejected.html`.

## Sending email messages

- Build a resolver (e.g., `StaticResolver`) that implements `ITokenResolver`, populate it with tokens your template expects (user object, displayName, dates, links, etc.).
- Call `messageService.sendMessage(<MessageTemplates constant>, resolver, new RecipientHolder(user))`; other overloads support multiple recipients.
- Example:

```java
StaticResolver data = new StaticResolver();
data.addToken("user", user);
data.addToken("displayName", user.getDisplayName());
data.addToken("expiryDate", Utils.formatDate(expiryDate));
data.addToken("resetUrl", resetUrl);

messageService.sendMessage(MessageTemplates.PASSWORD_EXPIRING, data, new RecipientHolder(user));
```

## Scheduled jobs (@TaskConfig, @ScheduledTaskConfig, @TenantTaskConfig)

- Implement `ScheduledTask` (and optionally `TenantTask` when multi-tenant or user-triggered) and annotate the class with:
  - `@TaskConfig`: core metadata (key, bundle/i18n, `affinity` for cluster placement, `onConflict` policy, optional `id`, `dontPersist`).
  - `@ScheduledTaskConfig`: cadence via cron string or `ScheduledTask` constants (e.g., `EVERY_MINUTE`, `AT_MIDNIGHT`); `systemOnly=true` hides it from tenant scheduling.
  - `@TenantTaskConfig`: UI/runtime flags such as `allowRunNow`, `allowTenantRunNow`, `logging`.
- I18n: add `<key>.name` and `<key>.desc` to the declared bundle (e.g., `express.properties`). Without these, the job won’t render cleanly in UI.
- Mark as `@Extension` so PF4J registers it; implement `execute()` and use `TaskContext.get().progress()` for progress messages.
- Example:

```java
@Extension
@TaskConfig(affinity = Affinity.ANY, bundle = MyBundle.RESOURCE_KEY, key = "myJob")
@ScheduledTaskConfig(ScheduledTask.AT_MIDNIGHT)
@TenantTaskConfig(allowRunNow = true, allowTenantRunNow = true)
public class MyJob implements TenantTask, ScheduledTask {
  public void execute() throws Exception { TaskContext.get().progress().message("myJob.start"); }
}
```

## Rendering rules (AbstractObjectRenderer highlights)

- Forms and tables are generated from `ObjectTemplate` and `TemplateView` definitions. If no views exist, a single dynamic view is created using all fields.
- `FieldView` (READ/CREATE/UPDATE) controls visibility; if a field’s views exclude the current scope, it is hidden (or removed for read-only display). `FieldRenderer.OPTIONAL` hides read-only empty fields.
- Collection fields: OBJECT_EMBEDDED collections render via `TableRenderer`; reference collections use `CollectionSearchFormInput`; ENUM collections use `MultipleSelectionFormInput`; TEXT with TAGS or COLLECTION render tag/collection inputs; ATTACHMENT uses multiple attachment input; OPTIONS uses options input against a referenced template.
- Reference selectors choose renderer based on `FieldRenderer` (DROPDOWN/DROPDOWN_MENU vs search modal). Meta `decorate=false` disables form adornments; meta `url` can override the search endpoint.
- Required fields set `required` on inputs; read-only fields set `readonly`. Hidden flags and `FieldOptions.SYSTEM_ONLY_VIEW` gate visibility.

## Services, database access, and dynamic columns

- Create a Spring service per entity by extending an `AbstractUUIDObjectService` variant. The common base `AbstractUUIDObjectServceImpl` injects `TenantAwareObjectDatabase` as `objectDatabase`; override `getResourceClass()` and add custom methods that compose `SearchField` constraints and call `objectDatabase` queries.
- Annotate the entity with @ObjectServiceBean to link the entity to the service. The bean attribute will point to the service class (if you created separate service implementation and interface, you should point to the interface, otherwise the service class).
- Override hooks when you need lifecycle logic: `beforeSave` and `afterSave` wrap `saveOrUpdate`; `beforeDelete` wraps `deleteObject`/`deleteObjectByUUID`; `setupDefaults` runs during `createNew`; `createIfNotExisting` is available to idempotently insert pre-seeded records (throws if UUID is blank).
- When extending `AbstractAssignableUUIDObjectServiceImpl`, always implement `createNew(ObjectTemplate)` yourself (it has no default); return a new instance of your resource class (e.g., `return new MyEntity();`).
- `TenantAwareObjectDatabase` helpers: `list`/`stream`, `get`/`getOr`, `saveOrUpdate`, `delete`/`deleteIfExists`, `count`, `searchObjects`/`searchTable`/`table`, `searchCount`, `sumLongValues`/`sumDoubleValues`, `countDistinct`, `max`/`min`, `stashObject`, `deleteAll`. Pass `SearchField` varargs to filter, or `SortOrder` + `sortField` where supported. Prefer `sumDoubleValues`/`countDistinct` for aggregates instead of iterating over collections.
- `SearchField` builders: `eq`, `not`, `like`, `in`, `all`, `gt`, `gte`, `lt`, `lte`, plus logical `or(...)` / `and(...)`; use `_id` (or `UUID`) for the primary key. `SearchField.add(...)` appends filters; `.process` lets a `SearchTransformer` rewrite values; `.mark` tags fields for downstream logic. Use collections helper `SearchField.filter(stream, filters)` for in-memory filtering.
- Use the extended service bases when they match your entity type: `AbstractAssignableUUIDObjectServiceImpl` (assignable objects via `AssignableObjectDatabase`), `AbstractPersonalUUIDObjectServceImpl` (per-user `PersonalUUIDEntity` with owner validation and personal database), and `AbstractUUIDObjectServceImpl` (default UUID entities). These wire the correct database abstraction and life-cycle hooks.
- Implement `DynamicColumnService` when dynamic columns are needed, and add `@DynamicColumn` to `@TableView` to surface those columns.

## Dashboard widgets

- Implement `DashboardWidget` with `@Extension`; pick a bucket via `BasicDashboardTypes` (e.g., `INSIGHTS` or `SERVER_INFORMATION`) and guard visibility with `PermissionService.assertAdministrator()` when appropriate.
- Resource mapping: place `SimpleName.html`, `SimpleName.css`, `SimpleName.js` (and optional `SimpleNameHelp.html`) in `src/main/resources/<package path>/`. Inline styles/`style` attributes are not allowed; use the CSS file.
- Build the widget UI with jsoup `Element` helpers (e.g., `Html.div`) and Bootstrap utility classes; add i18n via `jad:bundle`/`jad:i18n` attributes on elements.
- When injecting scripts, use `PageHelper.appendHeadScript` and loosen CSP via `SessionUtils.addContentSecurityPolicy` if inline scripts are required.


## Menus

- Prefer `@PageMenu` on the entity (or page/controller) to surface it in the navigation; only create a standalone `ApplicationMenu` when `@PageMenu` cannot cover the use case.

## Tenant bootstrap / default data

- Implement `TenantAware` on a Spring component to seed default data. Use `initializeTenant(Tenant tenant, boolean newSchema)` (or `initializeSystem` helper) and call your service’s `createIfNotExisting(...)` with a stable UUID on the seed object—this runs each startup, no `newSchema` guard needed.
- Assign defaults to required roles/users via services (e.g., `RoleService.getRoleByUUID(...)`) before invoking `createIfNotExisting`.
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
7. Consider `@GenerateEventTemplates` if auditing is desired or someone may need to extend behaviour via events.
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

## Caching
- Obtain local caches `Map<K,V>` from `CacheService.getCacheOrCreate` and `CacheService.clusteredCacheIfExists` methods (underlying cache is based on "Caffein").
- Obtain cluster wide shared caches of `Map<K,V>` from `CacheService.clusteredCacheOrCreate` and `CacheService.clusteredCacheIfExists`. The underlying mechanism uses JGroup. Not all `Map` methods are implemented, and some may be implemented inefficiently.

## Serialization In A Cluster
- Both cluster wide shared caches and the clustered scheduler need to serialize objects. JSON serialization is used. 
- All member variables of basic serialized objects must have getters and setters, and cannot be final. 
- Custom serialization and deserialization can be implemented using Jackson annotations. 
- For example of non-standard serialization see `OAuth2Request`.
- Serialization configuration happens in `SchedulerSpringConfig.createObjectMapper()`. Sometimes you will need to add new types here.
- Plugins can contribute custom serializers by implementing the `SchedulerSerializationProvider` interface. For example see `IpAddressSerialization` in the Nodal VPN project.

## Clustered Scheduler
- Whether the server is clustered or not does not matter, the scheduler treats the server as a cluster of at least one.
- The clustered scheduler handles "Jobs" (which are generally concrete classes) and "Tasks" which can be lambdas or unserializable `Runnable` or `Callable`.
- "Jobs" can run on any and all nodes, "Tasks" can only run on the local node.
- For this reason, any jobs that have any kind of parameters passed to them (usually in a constructor at construction time) must be entirely serializable.
- It is recommended you always create a separate class file for you job. Inner classes are fine, but make sure they are `public` and `static`.
- There are two main sub-types of jobs, "Scheduled Jobs" and "Ad-hoc Jobs". The base interface is `SerializableJob`, all jobs types implement this ultimately.
- All job types can be configured using the `@TaskConfig` annotation.
- "Scheduled Jobs" implement `ScheduledTask` and will be automatically scheduled on start-up of any node.
- All `ScheduledTask` implementations can be configured using the `@ScheduledTaskConfig` annotation.
- Most job types also implement `TenantTask`. This ensure the job is run in the context of the tenant it was created under. You nearly always want to do this.
- All `TenantTask` implementations can be configuration using `@TenantTaskConfig` annotation.
- "Ad-hoc" jobs must be manually triggered using one of the methods in `SchedulerService` using the `schedule` methods.
- "Tasks" should used `SchedulerService.scheduleIn`, `SchedulerService.runAs` or `SchedulerService.runNow`.


