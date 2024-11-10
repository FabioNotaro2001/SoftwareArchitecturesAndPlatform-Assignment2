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
        this.service = new RidesManagerVerticle(this.port, ridesAPI);
        Vertx v = Vertx.vertx();
        v.deployVerticle(service);
    }
}
