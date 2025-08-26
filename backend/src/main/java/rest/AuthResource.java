package rest;

import entity.User;
import database.PasswordUtil;
import database.TokenService;
import database.UserService;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @EJB
    private UserService userService;
    @Inject
    private TokenService tokenService;

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(User user) {
        User registeredUser = new User();
        registeredUser.setLogin(user.getLogin());
        registeredUser.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        registeredUser.setResults(new ArrayList<>());

        if (userService.findByUsername(registeredUser.getLogin()) != null) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("User with login " + registeredUser.getLogin() + " already exists")
                    .build();
        }

        if (userService.saveUserToDb(registeredUser)) {
            String token = tokenService.generateToken(registeredUser.getLogin());
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"token\":\"" + token + "\"}")
                    .build();
        } else {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while saving user")
                    .build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(User user) {
        User registeredUser = userService.findByUsername(user.getLogin());
        if (registeredUser == null) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("User not found")
                    .build();
        }

        if (!PasswordUtil.verifyPassword(user.getPassword(), registeredUser.getPassword())) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid password")
                    .build();
        }

        String token = tokenService.generateToken(registeredUser.getLogin());
        return Response
                .status(Response.Status.OK)
                .entity("{\"token\":\"" + token + "\"}")
                .build();
    }
}
