package sap.ass2.rides.infrastructure;

import io.vertx.core.Vertx;
import sap.ass2.rides.application.RidesManagerAPI;

public class RidesManagerController {
    private int port;
    private RidesManagerVerticle service;

    public RidesManagerController(int port){
        this.port = port;
    }
    
    public void init(RidesManagerAPI ridesAPI){
        Vertx vertx;
        if (Vertx.currentContext() != null) {
			vertx = Vertx.currentContext().owner();
		} else {
			vertx = Vertx.vertx();
		}
        this.service = new RidesManagerVerticle(this.port, ridesAPI);
        vertx.deployVerticle(this.service);
        ridesAPI.subscribeToRideEvents(this.service);
    }
}
