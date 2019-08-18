
       _           _             _   _           
      (_) __ _  __| | __ _ _ __ | |_(_)_   _____ 
      | |/ _` |/ _` |/ _` | '_ \| __| \ \ / / _ \
      | | (_| | (_| | (_| | |_) | |_| |\ V /  __/
     _/ |\__,_|\__,_|\__,_| .__/ \__|_| \_/ \___|
    |__/                  |_|                  

# Rapid, Adaptive REST API Service

The goal of this project is to create a web service for developers that enables rapid deployment of an entity-based REST API that implements standard CRUD and search operations without having to write code. All that should be required is a JSON template file that describes the entity required. The project will allow collections of resources to be persisted, managed and searched using a standard REST based API.

It is envisioned that an event model and simple plugin architecture will be provided to enable custom code to be injected in any part of the entity lifecycle and an eco system created to allow the sharing of templates and plugins between developers.

Key objectives for the alpha release:

- Persistence provider plugin interface.
- Implement persistence option for NoSQL databases.
- Implement CRUD REST operations.
- Implement search and list operations with paging support.
- Support for embedding of template defined entity within another entity
- Deploy as a WAR archive or startup in-built web server.

