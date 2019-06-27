package io.mateu.rest;


import com.google.common.base.Strings;
import io.mateu.mdd.core.util.Helper;
import io.mateu.model.Alumno;
import io.mateu.model.Asistencia;
import io.mateu.model.ClaseFecha;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("recurso")
public class Recurso {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "pong";
    }


    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> login(Map data) throws Throwable {
        String email = (String) data.get("email");
        String password = (String) data.get("password");
        System.out.println("email = " + email + ", password = " + password);
        Map<String, Object> rs = new HashMap();
        Helper.transact(em -> {
            rs.put("result", "error");
            if (Strings.isNullOrEmpty(email)) rs.put("msg", "Debes indicar una dirección de email");
            else {
                List<Alumno> l = em.createQuery("select x from " + Alumno.class.getName() + " x where lower(x.email) = :e").setParameter("e", email.toLowerCase()).getResultList();
                if (l.size() == 0) rs.put("msg", "No hay ningún alumno con el email " + email);
                else if (l.size() > 1) rs.put("msg", "Hay más de 1 alumno alumno con el email " + email);
                else {
                    Alumno a = l.get(0);
                    if (!a.isAppHabilitada()) rs.put("msg", "Acceso a la app no está habilitado para este alumno");
                    if (!a.isActivo()) rs.put("msg", "Este alumno no está activo");
                    if (a.getSaldo() < 0) rs.put("msg", "Alumno con saldo negativo");
                    if (!Helper.md5(password.toLowerCase()).equals(a.getPassword())) rs.put("msg", "Password incorrecto");
                    else {
                        rs.put("result", "ok");
                        rs.put("url", "/" + a.getAppId() + "/autenticado/");
                    }
                }
            }
        });
        return rs;
    }

    @POST
    @Path("resetpassword")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> resetPassword(Map data) throws Throwable {
        String idUsuario = (String) data.get("idUsuario");
        String email = (String) data.get("email");
        String password = (String) data.get("password");
        System.out.println("idUsuario = " + idUsuario + ", email = " + email + ", password = " + password);
        Map<String, Object> rs = new HashMap();
        Helper.transact(em -> {
            rs.put("result", "error");
            if (!Strings.isNullOrEmpty(idUsuario)) {
                long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
                System.out.println("id (decoded) = " + id);
                Alumno a = em.find(Alumno.class, id);
                rs.put("result", "error");
                if (a == null) rs.put("msg", "No existe ningún alumno con el id " + id);
                else {
                    a.enviarEmailResetPasswordApp();
                    rs.put("result", "ok");
                    rs.put("url", "/pwdresetsent");
                }

            } else if (Strings.isNullOrEmpty(email)) rs.put("msg", "Debes indicar una dirección de email");
            else {
                List<Alumno> l = em.createQuery("select x from " + Alumno.class.getName() + " x where lower(x.email) = :e").setParameter("e", email.toLowerCase()).getResultList();
                if (l.size() == 0) rs.put("msg", "No hay ningún alumno con el email " + email);
                else if (l.size() > 1) rs.put("msg", "Hay más de 1 alumno alumno con el email " + email);
                else {
                    Alumno a = l.get(0);
                    if (!a.isAppHabilitada()) rs.put("msg", "Acceso a la app no está habilitado para este alumno");
                    if (!a.isActivo()) rs.put("msg", "Este alumno no está activo");
                    if (a.getSaldo() < 0) rs.put("msg", "Alumno con saldo negativo");
                    else {
                        a.enviarEmailResetPasswordApp();
                        rs.put("result", "ok");
                        rs.put("url", "/pwdresetsent");
                    }
                }
            }
        });
        return rs;
    }

    @GET
    @Path("status/{idUsuario}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> status(@PathParam("idUsuario") String idUsuario) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        Map<String, Object> rs = new HashMap();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            rs.put("result", "error");
            if (a == null) rs.put("msg", "No existe ningún alumno con el id " + id);
            else {
                rs.put("nombre", a.getNombre());
                if (!a.isAppHabilitada()) rs.put("msg", "Acceso a la app no está habilitado para este alumno");
                else if (a.getSaldo() < 0) rs.put("msg", "Alumno con saldo negativo");
                else if (a.getPassword() == null) rs.put("result", "primeracceso");
                else rs.put("result", "ok");
            }
        });
        return rs;
    }

    @PUT
    @Path("password/{idUsuario}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> setPassword(@PathParam("idUsuario") String idUsuario, Map data) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id + "password = " + data.get("password"));
        Map<String, Object> rs = new HashMap();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            rs.put("result", "error");
            if (a == null) rs.put("msg", "No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) rs.put("msg", "Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) rs.put("msg", "Alumno con saldo negativo");
            else if (Strings.isNullOrEmpty((String) data.get("password"))) rs.put("msg", "El password no puede estar vacío");
            else {
                a.setPasswordResetKey(null);
                a.setPassword(Helper.md5((String) data.get("password")));
                rs.put("result", "ok");
            }
        });

        return rs;
    }

    @POST
    @Path("check/{idUsuario}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> check(@PathParam("idUsuario") String idUsuario, Map data) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id + "password = " + data.get("password"));
        Map<String, Object> rs = new HashMap();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            rs.put("result", "error");
            if (a == null) rs.put("msg", "No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) rs.put("msg", "Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) rs.put("msg", "Alumno con saldo negativo");
            else if (!Helper.md5(((String)data.get("password")).toLowerCase()).equals(a.getPassword())) rs.put("msg", "Password incorrecto");
            else {
                a.setUltimoAcceso(LocalDateTime.now());
                rs.put("result", "ok");
            }
        });
        return rs;
    }

    @GET
    @Path("datos/{idUsuario}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> datos(@PathParam("idUsuario") String idUsuario) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        Map<String, Object> rs = new HashMap();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            rs.put("result", "error");
            if (a == null) rs.put("msg", "No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) rs.put("msg", "Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) rs.put("msg", "Alumno con saldo negativo");
            else {
                rs.put("result", "ok");
                rs.put("activo", a.isActivo());
                rs.put("nombre", a.getNombre());
                rs.put("saldo", a.getSaldo());
                rs.put("nombre", a.getNombre());
            }
        });
        return rs;
    }

    @GET
    @Path("checkkey/{idUsuario}/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> checkKey(@PathParam("idUsuario") String idUsuario, @PathParam("key") String key) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        Map<String, Object> rs = new HashMap();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            rs.put("keyok", false);
            if (a == null) rs.put("msg", "No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) rs.put("msg", "Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) rs.put("msg", "Alumno con saldo negativo");
            else if (!key.equals(a.getPasswordResetKey())) rs.put("msg", "La key para resetear el password ya no es válida");
            else {
                rs.put("keyok", true);
            }
        });
        return rs;
    }

    @GET
    @Path("clases/{idUsuario}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> clases(@PathParam("idUsuario") String idUsuario) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        List<Map<String, Object>> rs = new ArrayList<>();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            if (a == null) throw new Exception("No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) throw new Exception("Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) throw new Exception("Alumno con saldo negativo");
            else {
                a.getAsistencias().stream().sorted((a1, a2) -> a1.getClase().getFecha().compareTo(a2.getClase().getFecha())).forEach(as -> {
                    rs.add((Map<String, Object>) getData(as));
                });
            }
        });
        return rs;
    }

    @GET
    @Path("clases/{idUsuario}/{idClase}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> clase(@PathParam("idUsuario") String idUsuario, @PathParam("idClase") long idClase) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        Map<String, Object> rs = new HashMap<>();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            if (a == null) throw new Exception("No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) throw new Exception("Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) throw new Exception("Alumno con saldo negativo");
            else {

                Asistencia as = em.find(Asistencia.class, idClase);
                rs.putAll(getData(as));

            }
        });
        return rs;
    }


    @DELETE
    @Path("clases/{idUsuario}/{idClase}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> anularClase(@PathParam("idUsuario") String idUsuario, @PathParam("idClase") long idClase) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        Map<String, Object> rs = new HashMap<>();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            if (a == null) throw new Exception("No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) throw new Exception("Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) throw new Exception("Alumno con saldo negativo");
            else {

                Asistencia as = em.find(Asistencia.class, idClase);
                as.setActiva(false);

                rs.putAll(getData(as));

            }
        });
        return rs;
    }

    @PUT
    @Path("clases/{idUsuario}/{idClase}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> restaurarClase(@PathParam("idUsuario") String idUsuario, @PathParam("idClase") long idClase) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        Map<String, Object> rs = new HashMap<>();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            if (a == null) throw new Exception("No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) throw new Exception("Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) throw new Exception("Alumno con saldo negativo");
            else {

                Asistencia as = em.find(Asistencia.class, idClase);
                as.setActiva(true);

                rs.putAll(getData(as));

            }
        });
        return rs;
    }

    private Map<? extends String,?> getData(Asistencia as) {
        return Helper.hashmap("fecha", as.getClase().getFecha().format(DateTimeFormatter.ISO_DATE)
                , "id", as.getId()
                , "hora", as.getClase().getClase().getSlot().toString()
                , "activa", as.isActiva()
                , "cambio", !as.getClaseOriginal().equals(as.getClase())
                , "actividad", as.getClase().getClase().getActividad().toString()
                , "nivel", as.getClase().getClase().getNivel().toString()
                , "modificable", LocalDateTime.now().isBefore(as.getClase().getFecha().atTime(as.getClase().getClase().getSlot().getFranja().getDesde()).minusHours(4)) || !as.isActiva()
        );
    }

    @POST
    @Path("clases/{idUsuario}/{idClase}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> cambiarClase(@PathParam("idUsuario") String idUsuario, @PathParam("idClase") long idClase, Map data) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        Map<String, Object> rs = new HashMap<>();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            if (a == null) throw new Exception("No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) throw new Exception("Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) throw new Exception("Alumno con saldo negativo");
            else {

                Asistencia as = em.find(Asistencia.class, idClase);
                as.getClase().getAsistencias().remove(as);
                as.setClase(em.find(ClaseFecha.class, ((Integer) data.get("nuevaClase")).longValue()));
                if (!as.getClase().getAsistencias().contains(as)) as.getClase().getAsistencias().add(as);
                as.setActiva(true);

                rs.putAll(getData(as));

            }
        });
        return rs;
    }

    @GET
    @Path("alternativas/{idUsuario}/{idClase}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> fechasConPlazas(@PathParam("idUsuario") String idUsuario, @PathParam("idClase") long idClase) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        List<Map<String, Object>> rs = new ArrayList<>();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            if (a == null) throw new Exception("No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) throw new Exception("Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) throw new Exception("Alumno con saldo negativo");
            else {


                Asistencia as = em.find(Asistencia.class, idClase);

                as.getAlternativas().stream().map(c -> c.getFecha()).distinct().sorted().forEach(c -> {
                    rs.add(Helper.hashmap("fecha", c.format(DateTimeFormatter.ISO_DATE), "texto", c.format(DateTimeFormatter.ofPattern("EEEE dd MMMM"))
                    ));
                });
            }
        });
        return rs;
    }

    @GET
    @Path("alternativas/{idUsuario}/{idClase}/{fecha}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> alternativas(@PathParam("idUsuario") String idUsuario, @PathParam("idClase") long idClase, @PathParam("fecha") String fecha) throws Throwable {
        System.out.println("idUsuario = " + idUsuario);
        long id = Long.parseLong(new String(Base64.getDecoder().decode(idUsuario)));
        System.out.println("id (decoded) = " + id);
        List<Map<String, Object>> rs = new ArrayList<>();
        Helper.transact(em -> {
            Alumno a = em.find(Alumno.class, id);
            if (a == null) throw new Exception("No existe ningún alumno con el id " + id);
            else if (!a.isAppHabilitada()) throw new Exception("Acceso a la app no está habilitado para este alumno");
            else if (a.getSaldo() < 0) throw new Exception("Alumno con saldo negativo");
            else {


                Asistencia as = em.find(Asistencia.class, idClase);

                LocalDate f = LocalDate.parse(fecha, DateTimeFormatter.ISO_DATE);

                as.getAlternativas().stream().filter(c -> c.getFecha().equals(f)).forEach(c -> {
                    rs.add(Helper.hashmap("fecha", c.getFecha().format(DateTimeFormatter.ISO_DATE)
                            , "id", c.getId()
                            , "hora", c.getClase().getSlot().getFranja().toString()
                            , "actividad", c.getClase().getActividad().toString()
                            , "nivel", c.getClase().getNivel().toString()
                    ));
                });
            }
        });
        return rs;
    }

}
