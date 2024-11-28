package sap.ass2.registry.infrastructure;

import java.util.logging.Logger;
import io.vertx.core.Vertx;
import sap.ass2.registry.domain.RegistryAPI;

// @RestController
// @RequestMapping("/registry")
public class RegistryController {
    private int port;
    private RegistryVerticle service;
    // private RegistryAPI registryAPI;

    static Logger logger = Logger.getLogger("[Registry Controller]");	

    public RegistryController(int port){
        this.port = port;
    }
    
    public void init(RegistryAPI registryAPI){
        // this.registryAPI = registryAPI;
        this.service = new RegistryVerticle(this.port, registryAPI);
        Vertx v = Vertx.vertx();
        v.deployVerticle(service);
    }

    // @PostMapping(
    //     value = { "/users-manager" }, 
    //     produces = "application/json",
    //     consumes = "application/json"
    //     )
    // @ResponseBody
    // public JsonObject registerUsersManager(@RequestBody JsonObject data) throws MalformedURLException {
    //     logger.log(Level.INFO, "Received 'registerUsersManager'");

    //     String name = data.getString("name");
    //     String address = data.getString("address");

    //     var url = URI.create(address).toURL();
    //     this.registryAPI.registerUsersManager(name, url);
    //     return new JsonObject();
    // }

    // protected void registerEbikesManager(RoutingContext context) {
    //     logger.log(Level.INFO, "Received 'registerEbikesManager'");

    //     context.request().handler(buffer -> {
    //         JsonObject data = buffer.toJsonObject();
    //         String name = data.getString("name");
	// 		String address = data.getString("address");			

    //         JsonObject response = new JsonObject();
    //         try {
    //             var url = URI.create(address).toURL();
    //             this.registryAPI.registerEbikesManager(name, url);
    //             sendReply(context.response(), response);
    //         } catch (Exception ex) {
    //             sendServiceError(context.response(), ex);
    //         }
    //     });
    // }

    // protected void registerRidesManager(RoutingContext context) {
    //     logger.log(Level.INFO, "Received 'registerRidesManager'");

    //     context.request().handler(buffer -> {
    //         JsonObject data = buffer.toJsonObject();
    //         String name = data.getString("name");
	// 		String address = data.getString("address");			

    //         JsonObject response = new JsonObject();
    //         try {
    //             var url = URI.create(address).toURL();
    //             this.registryAPI.registerRidesManager(name, url);
    //             sendReply(context.response(), response);
    //         } catch (Exception ex) {
    //             sendServiceError(context.response(), ex);
    //         }
    //     });
    // }

    // @GetMapping(
    //     value = { "/users-manager" }, 
    //     produces = "application/json",
    //     consumes = "application/json"
    // )
    // @ResponseBody
    // protected ResponseEntity<JsonObject> lookupUsersManager(@RequestParam("usersManagerName") String name) {
    //     logger.log(Level.INFO, "Received 'lookupUsersManager'");

    //     JsonObject response = new JsonObject();
    //     // String name = context.pathParam("usersManagerName");
    //     try {
    //         var usersManagerOpt = this.registryAPI.lookupUsersManager(name);
    //         if (usersManagerOpt.isPresent()) {
    //             response.put("usersManager", usersManagerOpt.get());
    //         }
    //         return ResponseEntity.ok(response);
    //         // sendReply(context.response(), response);
    //     } catch (Exception ex) {
    //         // sendServiceError(context.response(), ex);
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }

    // protected void lookupEbikesManager(RoutingContext context) {
    //     logger.log(Level.INFO, "Received 'lookupEbikesManager'");

    //     JsonObject response = new JsonObject();
    //     String name = context.pathParam("ebikesManagerName");
    //     try {
    //         var ebikesManagerOpt = this.registryAPI.lookupEbikesManager(name);
    //         if (ebikesManagerOpt.isPresent()) {
    //             response.put("ebikesManager", ebikesManagerOpt.get());
    //         }
    //         sendReply(context.response(), response);
    //     } catch (Exception ex) {
    //         sendServiceError(context.response(), ex);
    //     }
    // }

    // protected void lookupRidesManager(RoutingContext context) {
    //     logger.log(Level.INFO, "Received 'lookupRidesManager'");

    //     JsonObject response = new JsonObject();
    //     String name = context.pathParam("ridesManagerName");
    //     try {
    //         var ridesManagerOpt = this.registryAPI.lookupRidesManager(name);
    //         if (ridesManagerOpt.isPresent()) {
    //             response.put("ridesManager", ridesManagerOpt.get());
    //         }
    //         sendReply(context.response(), response);
    //     } catch (Exception ex) {
    //         sendServiceError(context.response(), ex);
    //     }
    // }
}
