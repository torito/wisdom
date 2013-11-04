package org.ow2.chameleon.wisdom.samples.session;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.wisdom.api.Controller;
import org.ow2.chameleon.wisdom.api.DefaultController;
import org.ow2.chameleon.wisdom.api.annotations.Route;
import org.ow2.chameleon.wisdom.api.http.HttpMethod;
import org.ow2.chameleon.wisdom.api.http.Result;
import org.ow2.chameleon.wisdom.api.router.Router;
import org.ow2.chameleon.wisdom.api.templates.Template;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * A simple controller to demonstrate sessions.
 */
@Component
@Provides(specifications = Controller.class)
@Instantiate
public class SessionController extends DefaultController {

    @Requires(filter = "(name=session/session)")
    public Template index;

    @Route(method = HttpMethod.GET, uri = "/session")
    public Result index() {
        System.out.println(session().getData());
        Map<String, String> data = session().getData();
        return ok(render(index, "data", data));
    }

    @Requires
    public Router router;

    /**
     * Action called to clear the session
     */
    @Route(method = HttpMethod.POST, uri = "/session/clear")
    public Result clear() {
        session().clear();
        return redirect(router.getReverseRouteFor(this, "index"));
    }

    /**
     * Action called to populate the session
     */
    @Route(method = HttpMethod.POST, uri = "/session/populate")
    public Result populate() {
        session().put("createdBy", "wisdom");
        session().put("at", DateFormat.getDateTimeInstance().format(new Date()));
        return redirect(router.getReverseRouteFor(this, "index"));
    }

}