## Webflux & Cache

Spring Webflux is the non-blocking version of Spring Web MVC 
built upon project-reactor. Webflux provides capability 
for handling concurrency with a small number of 
threads and scale with less hardware resources.
For now, there is no integration of @Cacheable with Reactor 3.
However, you may bypass that thing by using aop.  

Caching can significantly improve performance in a microservices environment, 
wenn high latency is required. Caching can also help with resilience.
This is a demo which shows how to use 
Infinispan embedded in your webflux application in a reactive manner leveraging spring aop. 
Another caching solutions can be use in the same way. 

### Running the application

You can run your application using docker:
```
docker-compose up --build
```
