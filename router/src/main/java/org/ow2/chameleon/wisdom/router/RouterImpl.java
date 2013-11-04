package org.ow2.chameleon.wisdom.router;

import com.google.common.collect.Maps;
import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.router.AbstractRouter;
import org.ow2.chameleon.wisdom.api.router.Route;
import org.ow2.chameleon.wisdom.api.router.RouteUtils;
import org.ow2.chameleon.wisdom.api.router.RoutingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The route.
 */
@Component
@Provides
@Instantiate(name = "router")
public class RouterImpl extends AbstractRouter {

    private static Logger logger = LoggerFactory.getLogger(RouterImpl.class);
    private Set<Route> routes = new LinkedHashSet<Route>();

    @Bind(aggregate = true)
    public synchronized void bindController(Controller controller) {
        logger.info("Adding routes from " + controller);

        List<Route> annotatedNewRoutes = RouteUtils.collectRouteFromControllerAnnotations(controller);
        Set<Route> newRoutes = new LinkedHashSet<Route>();
        newRoutes.addAll(annotatedNewRoutes);
        newRoutes.addAll(controller.routes());

        try {
            //check if these new routes don't pre-exist
            ensureNoConflicts(newRoutes, routes);
        } catch (RoutingException e) {
            logger.error("The controller {} declares routes conflicting with existing routes, " +
                    "the controller is ignored, reason: {}", controller, e.getMessage());
            return;
        }

        // if new routes are clean add all routes
        routes.addAll(newRoutes);
    }

    private void ensureNoConflicts(Set<Route> newRoutes, Set<Route> existingRoutes) {
        //check if these new routes don't pre-exist in existingRoutes

        for (Route newRoute : newRoutes) {
            for (Route existingRoute : existingRoutes) {
                boolean sameHttpMethod = (existingRoute.getHttpMethod().equals(newRoute.getHttpMethod()));
                boolean sameUrl = (existingRoute.getUrl().equals(newRoute.getUrl()));

                // very same route or same url and method.
                if (existingRoute.equals(newRoute)  || (sameUrl  && sameHttpMethod)) {
                    throw new RoutingException(newRoute.getHttpMethod() + " " + newRoute.getUrl()
                            + " is already registered in controller " + existingRoute.getControllerClass());
                }
            }

            // this routes seems to be clean, store it
            routes.add(newRoute);
        }
    }

    @Unbind
    public synchronized void unbindController(Controller controller) {
        logger.info("Removing routes from " + controller);
        routes.removeAll(controller.routes());
        routes.removeAll(RouteUtils.collectRouteFromControllerAnnotations(controller));
    }

    @Validate
    public void start() {
        logger.info("Router starting");
    }

    @Invalidate
    public void stop() {
        logger.info("Router stopping");
        routes.clear();
    }

    private synchronized Set<Route> copy() {
        return new LinkedHashSet<Route>(routes);
    }

    @Override
    public Route getRouteFor(HttpMethod method, String uri) {
        for (Route route : copy()) {
            if (route.matches(method, uri)) {
                return route;
            }
        }
        return null;
    }

    @Override
    public String getReverseRouteFor(String className, String method, Map<String, Object> params) {
        for (Route route : copy()) {

            if (route.getControllerClass().getName().equals(className)
                    && route.getControllerMethod().getName().equals(method)) {

                return computeUrlForRoute(route, params);
            }
        }
        return null;
    }

    @Override
    public Collection<Route> getRoutes() {
        return copy();
    }

    private String computeUrlForRoute(Route route, Map<String, Object> params) {
        if (params == null) {
            // No variables, return the raw url.
            return route.getUrl();
        }

        // The original url. Something like route/user/{id}/{email}/userDashboard
        String urlWithReplacedPlaceholders = route.getUrl();

        Map<String, Object> queryParameterMap = Maps.newHashMap();

        for (Map.Entry<String, Object> entry : params.entrySet()) {

            // The original regex. For the example above this results in {id}
            String originalRegex = String.format("{%s}", entry.getKey());
            String originalRegexEscaped = String.format("\\{%s\\}", entry.getKey());

            // The value that will be added into the regex => myId for instance...
            String resultingRegexReplacement = entry.getValue().toString();

            // If regex is in the url as placeholder we replace the placeholder
            if (urlWithReplacedPlaceholders.contains(originalRegex)) {

                urlWithReplacedPlaceholders = urlWithReplacedPlaceholders.replaceAll(
                        originalRegexEscaped,
                        resultingRegexReplacement);

                // If the parameter is not there as placeholder we add it as queryParameter
            } else {
                queryParameterMap.put(entry.getKey(), entry.getValue());
            }
        }

        // now prepare the query string for this url if we got some query params
        if (queryParameterMap.entrySet().size() > 0) {

            StringBuilder queryParameterStringBuffer = new StringBuilder();

            // The uri is now replaced => we now have to add potential query parameters
            for (Iterator<Map.Entry<String, Object>> iterator = queryParameterMap.entrySet().iterator();
                 iterator.hasNext(); ) {

                Map.Entry<String, Object> queryParameterEntry = iterator.next();
                queryParameterStringBuffer.append(queryParameterEntry.getKey());
                queryParameterStringBuffer.append("=");
                queryParameterStringBuffer.append(queryParameterEntry.getValue());

                if (iterator.hasNext()) {
                    queryParameterStringBuffer.append("&");
                }

            }

            urlWithReplacedPlaceholders = urlWithReplacedPlaceholders
                    + "?"
                    + queryParameterStringBuffer.toString();

        }


        return urlWithReplacedPlaceholders;
    }
}

