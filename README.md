## Webflux & Cache

Spring Webflux is the non-blocking version of Spring Web MVC 
built upon project-reactor. Webflux provides capability 
for handling concurrency with a small number of 
threads and scale with less hardware resources. Contrary to Spring Web MVC
spring doesn't yet provides caching out the box for webflux.

This is a demo which shows how to use 
Infinispan embedded in your webflux application in a reactive manner. 
Another caching solutions can be use in the same way. 

### Running the application

You can run your application using docker:
```
docker-compose up --build
```