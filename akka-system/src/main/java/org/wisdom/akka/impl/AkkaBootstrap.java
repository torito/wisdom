package org.wisdom.akka.impl;

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.akka.AkkaSystemService;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.Result;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorSystem;
import akka.osgi.OsgiActorSystemFactory;

import com.typesafe.config.ConfigFactory;

@Component
@Provides
@Instantiate
public class AkkaBootstrap implements AkkaSystemService {

    private final BundleContext context;
    private ServiceRegistration<ActorSystem> systemRegistration;
    private ActorSystem system;

    public AkkaBootstrap(BundleContext context) {
        this.context = context;
    }

    @Validate
    public void start() {
        OsgiActorSystemFactory osgiActorSystemFactory = OsgiActorSystemFactory.apply(context, ConfigFactory.empty());
        system = osgiActorSystemFactory.createActorSystem("wisdom-system");

        systemRegistration = context.registerService(ActorSystem.class, system, null);
    }

    @Invalidate
    public void stop() {
        unregisterQuietly(systemRegistration);
        systemRegistration = null;
        system.shutdown();
    }

    private void unregisterQuietly(ServiceRegistration<?> registration) {
        try {
            registration.unregister();
        } catch (Exception e) { //NOSONAR
            // Ignored.
        }
    }

    @Override
    public ActorSystem system() {
        return system;
    }

    @Override
    public Future<Result> dispatchResultWithContext(Callable<Result> callable, Context context) {
        return akka.dispatch.Futures.future(callable,
                new HttpExecutionContext(system.dispatcher(), context, Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public Future<Result> dispatchResult(Callable<Result> callable) {
        return akka.dispatch.Futures.future(callable,
                new HttpExecutionContext(system.dispatcher(), Context.CONTEXT.get(),
                        Thread.currentThread().getContextClassLoader
                                ()));
    }
    
    @Override
    public Future<InputStream> dispatchInputStream(Callable<InputStream> callable) {
        return akka.dispatch.Futures.future(callable,
                new HttpExecutionContext(system.dispatcher(), Context.CONTEXT.get(),
                        Thread.currentThread().getContextClassLoader
                                ()));
    }

    public ExecutionContext fromThread() {
        return new HttpExecutionContext(system.dispatcher(), Context.CONTEXT.get(),
                Thread.currentThread().getContextClassLoader());
    }
}
