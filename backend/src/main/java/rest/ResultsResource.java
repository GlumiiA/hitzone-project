package rest;

import entity.Result;
import entity.StatsDTO;
import entity.User;
import database.ResultService;
import database.TokenService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mbeans.ClickStats;
import mbeans.MissRatio;
import validation.Validator;

import java.util.Objects;

@Path("/results")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResultsResource {
    @EJB
    private TokenService tokenService;
    @EJB
    private ResultService resultService;

    @Inject
    private MBeanRegistrar mbeanRegistrar;

    @POST
    @Path("/addClick")
    public Response addClick(@QueryParam("hit") boolean hit) {
//        mbeanRegistrar.getClickStats().addClick(hit);
        return Response.ok("Click added").build();
    }

    @POST
    public Response addResult (
            @HeaderParam("Authorization") String authHeader,
            Result dot) {
        long startTime = System.currentTimeMillis();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Token not found")
                    .build();
        }

        String token = authHeader.substring("Bearer ".length());
        User user = tokenService.getUserFromToken(token);
        if (Objects.isNull(user)) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("User not found")
                    .build();
        }

        double x = dot.getX(), y = dot.getY(), r = dot.getR();
        if (!new Validator(x, y, r).validateDot())
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Invalid parameters")
                    .build();

        Result result = new Result(x, y, r);
        boolean isHit = result.isHit();
        System.out.printf("Point: x=%.2f y=%.2f r=%.2f → hit=%b%n",
                dot.getX(), dot.getY(), dot.getR(), isHit);
        mbeanRegistrar.getClickStats().addClick(isHit);
        result.setScriptTime(System.currentTimeMillis() - startTime);
        result.setStartTime(startTime);
        user.addResult(result);
        if (resultService.saveResultToDb(result)) {
            return Response
                    .status(Response.Status.OK)
                    .entity(result.toJSONObject())
                    .build();
        } else {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while saving result")
                    .build();
        }
    }

    @DELETE
    public Response clearResults(@HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Token not found")
                    .build();
        }

        String token = authHeader.substring("Bearer ".length());
        User user = tokenService.getUserFromToken(token);
        if (Objects.isNull(user)) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("User not found")
                    .build();
        }

        try {
            resultService.clearResultsForUser(user);
            user.clearResults();
            return Response
                    .status(Response.Status.OK)
                    .entity("All results cleared successfully")
                    .build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while clearing results")
                    .build();
        }
    }

    @GET
    public Response getResults(@HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Token not found")
                    .build();
        }

        String token = authHeader.substring("Bearer ".length());
        User user = tokenService.getUserFromToken(token);
        if (Objects.isNull(user)) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("User not found")
                    .build();
        }

        try {
            return Response
                    .status(Response.Status.OK)
                    .entity(user.getResultsAsJSON())
                    .build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while fetching results")
                    .build();
        }
    }

    @Path("/userStats")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserStats(@HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Token not found")
                    .build();
        }

        String token = authHeader.substring("Bearer ".length());
        User user = tokenService.getUserFromToken(token);
        if (Objects.isNull(user)) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("User not found")
                    .build();
        }

        try {
            String login = user.getLogin();
            String totalResults = String.valueOf(user.getResults().toArray().length);
            String hits = String.valueOf(user.getResults().stream().filter(Result::isHit).count());
            String misses = String.valueOf(user.getResults().stream().filter(r -> !r.isHit()).count());
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"login\":\"" + login + "\",\"totalResults\":\"" + totalResults + "\",\"hits\":\"" + hits + "\",\"misses\":\"" + misses + "\"}")
                    .build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while fetching stats")
                    .build();
        }
    }

    @GET
    @Path("/clickStats")
    public Response getClickStats() {
        ClickStats stats = mbeanRegistrar.getClickStats();
        MissRatio ratio = mbeanRegistrar.getMissRatio();

        return Response.ok(new StatsDTO(
                stats.getTotalClicks(),
                stats.getMisses(),
                ratio.getMissRatio()
        )).build();
    }
}
