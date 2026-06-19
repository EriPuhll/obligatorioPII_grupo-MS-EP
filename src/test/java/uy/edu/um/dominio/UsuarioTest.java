package uy.edu.um.dominio;

import org.junit.jupiter.api.Test;
import uy.edu.um.exceptions.DatoInvalidoException;
import uy.edu.um.exceptions.TipoUsuarioInvalidoException;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    public void crearUsuarioAdminCorrectamente() throws Exception {
        Usuario usuario = new Usuario(1, "Hera", TipoUsuario.ADMIN);

        assertEquals(1, usuario.getUid());
        assertEquals("Hera", usuario.getAlias());
        assertEquals(TipoUsuario.ADMIN, usuario.getTipo());
        assertEquals(32, usuario.getPeso());
    }

    @Test
    public void crearUsuarioGenericCorrectamente() throws Exception {
        Usuario usuario = new Usuario(2, "Zeus", TipoUsuario.GENERIC);

        assertEquals(2, usuario.getUid());
        assertEquals("Zeus", usuario.getAlias());
        assertEquals(TipoUsuario.GENERIC, usuario.getTipo());
        assertEquals(16, usuario.getPeso());
    }

    @Test
    public void noPermiteUidNegativo() {
        assertThrows(DatoInvalidoException.class, () -> {
            new Usuario(-1, "Hera", TipoUsuario.ADMIN);
        });
    }

    @Test
    public void noPermiteAliasVacio() {
        assertThrows(DatoInvalidoException.class, () -> {
            new Usuario(1, "", TipoUsuario.ADMIN);
        });
    }

    @Test
    public void noPermiteTipoNulo() {
        assertThrows(TipoUsuarioInvalidoException.class, () -> {
            new Usuario(1, "Hera", (TipoUsuario) null);
        });
    }
}