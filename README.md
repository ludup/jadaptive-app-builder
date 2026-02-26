
       _           _             _   _           
      (_) __ _  __| | __ _ _ __ | |_(_)_   _____ 
      | |/ _` |/ _` |/ _` | '_ \| __| \ \ / / _ \
      | | (_| | (_| | (_| | |_) | |_| |\ V /  __/
     _/ |\__,_|\__,_|\__,_| .__/ \__|_| \_/ \___|
    |__/                  |_|                  

# Adaptive Application Builder
The Adaptive Application Builder is a plugin-first platform for building secure, multi-tenant applications. Entities are annotated POJOs stored in MongoDB; templates generate CRUD UI, validation, and permissions automatically. Plugins package features behind PF4J with Spring wiring, keeping dependencies isolated per module while sharing common runtime services.

Key docs:
- [DEVELOPER_SETUP_GUIDE.md](DEVELOPER_SETUP_GUIDE.md) environment setup and tooling.
- [agent.md](.github/agents/jadaptive.agent.md)�� concise rules for entities, services, controllers, plugins, and events.
- [DEVELOPER.md](DEVELOPER.md) in-depth manual with patterns, examples, and best practices.

What the framework provides
- Auto-generated CRUD UI and REST for annotated entities, with search, validation, i18n, and permissions baked in.
- Strong plugin structure via PF4J + Spring: each module ships as an isolated plugin, declares its dependencies, and can extend others safely.
- Extension mechanisms: PF4J `@Extension` points, `@DynamicColumn` for tables, events for lifecycle hooks, and custom pages with `HtmlPage`.
- Tenant-aware persistence and permission context helpers to keep multi-tenant concerns consistent.

Why use it
- Build features quickly with minimal boilerplate; focus on domain logic while the platform supplies UI, storage, and security defaults.
- Encapsulate features per plugin, enabling modular deployments and safer dependency management.
- Extensible by design: add behaviours via extension points, events, and custom controllers without forking core code.


