
       _           _             _   _           
      (_) __ _  __| | __ _ _ __ | |_(_)_   _____ 
      | |/ _` |/ _` |/ _` | '_ \| __| \ \ / / _ \
      | | (_| | (_| | (_| | |_) | |_| |\ V /  __/
     _/ |\__,_|\__,_|\__,_| .__/ \__|_| \_/ \___|
    |__/                  |_|                  

# Rapid, Adaptive REST API Builder for Java

The goal of this project is to create a web service for Java developers that enables rapid deployment of a resource-based REST API that implements standard CRUD operations without having to write a line of code. All that should be required is a JSON template file that describes the resource required. The project will allow collections of resources to be persisted, managed and searched using a standard REST based API.

An event model and simple plugin architecture will be provided to enable custom code to be injected in any part of the resource lifecycle and an eco system created to allow the sharing of resource templates and plugins between developers.

Key objectives for the alpha release:

- Multiple persistence provider plugins to enable storage in either relational or NoSQL type databases.
- Deploy as a WAR archive or startup in-built web server.
- Event model for Create, Read, Update, Delete

