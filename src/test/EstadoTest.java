import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests de la Tarea 1 contra doc/specs/spec-estado.md (CA-1.1 .. CA-1.13).
 */
class EstadoTest {

    private static final String RESUELTO = "00010203040506070809101112131415";

    private static Estado resuelto() {
        return new Estado(RESUELTO);
    }

    // --- CA-1.1, CA-1.4 ------------------------------------------------------

    @Test
    void ca11CadenaDe32DigitosResueltaEsResuelto() {
        assertTrue(resuelto().esResuelto());
        assertFalse(new Estado("12000102030506070409101108131415").esResuelto());
    }

    @Test
    void ca14BitboardDelResueltoEsLaConstante() {
        assertEquals(0xFEDCBA98_76543210L, resuelto().bitboard());
    }

    // --- CA-1.2 --------------------------------------------------------------

    @Test
    void ca12ToStringProduce32DigitosYRoundTrip() {
        Estado e = resuelto();
        assertEquals(RESUELTO, e.toString());
        assertEquals(32, e.toString().length());
        assertEquals(e, new Estado(e.toString()));
    }

    @Test
    void ca12FichasMenoresQueDiezVanConCeroInicial() {
        assertEquals("00", resuelto().toString().substring(0, 2));
        assertEquals("15", resuelto().toString().substring(30));
    }

    // --- CA-1.3 --------------------------------------------------------------

    @Test
    void ca13FichaIndiceYCoordenadasCoinciden() {
        Estado e = resuelto();
        for (int i = 0; i < 16; i++) {
            assertEquals(i, e.ficha(i), "ficha(" + i + ")");
            assertEquals(e.ficha(i), e.ficha(i / 4, i % 4), "coordenadas de " + i);
        }
    }

    @Test
    void ca13FichaEnEstadoNoResuelto() {
        Estado e = new Estado("12000102030506070409101108131415");
        assertEquals(12, e.ficha(0, 0));
        assertEquals(3, e.ficha(1, 0));
        assertEquals(8, e.ficha(3, 0));
        assertEquals(15, e.ficha(3, 3));
    }

    // --- CA-1.5 --------------------------------------------------------------

    @Test
    void ca15SucesoresTiene32EntradasConAccionesYCosteUnitario() {
        List<Sucesor> suc = resuelto().sucesores();
        assertEquals(32, suc.size());
        for (int i = 0; i < 32; i++) {
            assertEquals(Estado.ACCIONES[i], suc.get(i).accion(), "accion " + i);
            assertEquals(1.0f, suc.get(i).costo(), "coste " + i);
        }
    }

    @Test
    void ca15TodosLosSucesoresCambianElEstado() {
        Estado e = resuelto();
        for (Sucesor s : e.sucesores()) {
            assertNotEquals(e, s.estado(), "el sucesor no cambia el estado");
        }
    }

    // --- CA-1.6 --------------------------------------------------------------

    @Test
    void ca16AplicarNoModificaElEstadoReceptor() {
        Estado e = resuelto();
        Estado copia = resuelto();
        e.aplicar(Estado.accionDesde("00+"));
        e.aplicar(Estado.accionDesde("33-"));
        assertEquals(copia, e, "el estado original queda intacto");
    }

    @Test
    void ca16TodaAccionTieneOrdenSiete() {
        Estado e = resuelto();
        for (int a = 0; a < 32; a++) {
            Estado x = e;
            for (int k = 0; k < 7; k++) {
                x = x.aplicar(Estado.ACCIONES[a]);
            }
            assertEquals(e, x,
                    "a^7 = identidad para " + Estado.accionComoTexto(Estado.ACCIONES[a]));
        }
    }

    @Test
    void ca16LaInversaDeUnaAccionEsAplicarlaSeisVeces() {
        Estado e = resuelto();
        for (int a = 0; a < 32; a++) {
            Estado inv = e.aplicar(Estado.ACCIONES[a]);
            for (int k = 0; k < 6; k++) {
                inv = inv.aplicar(Estado.ACCIONES[a]);
            }
            assertEquals(e, inv,
                    "a^6 invierte a para " + Estado.accionComoTexto(Estado.ACCIONES[a]));
        }
    }

    @Test
    void ca16ElSignoInvertidoNoEsLaInversa() {
        Estado e = resuelto();
        Estado directa = e.aplicar(Estado.accionDesde("00+"));
        assertNotEquals(e, directa.aplicar(Estado.accionDesde("00-")),
                "00+ seguido de 00- NO vuelve al original (fila y columna no conmutan)");
    }

    // --- CA-1.7, CA-1.8 ------------------------------------------------------

    @Test
    void ca17ca18CeroMasDesplazaFilaALaDerechaYColumnaHaciaAbajo() {
        Estado resultante = resuelto().aplicar(Estado.accionDesde("00+"));
        assertEquals("12000102030506070409101108131415", resultante.toString());
        assertEquals(12, resultante.ficha(0, 0), "fila 0 primero y columna 0 despues");
        assertEquals(0, resultante.ficha(0, 1));
        assertEquals(3, resultante.ficha(1, 0));
        assertEquals(4, resultante.ficha(2, 0));
    }

    @Test
    void ca17MenosDesplazaFilaALaIzquierdaYColumnaHaciaArriba() {
        Estado resultante = resuelto().aplicar(Estado.accionDesde("00-"));
        assertEquals(4, resultante.ficha(0, 0), "columna 0 sube tras rotar fila a la izquierda");
        assertEquals(2, resultante.ficha(0, 1));
        assertEquals(8, resultante.ficha(1, 0));
        assertEquals(1, resultante.ficha(3, 0));
        assertEquals(0, resultante.ficha(0, 3));
    }

    // --- CA-1.9, CA-1.10 -----------------------------------------------------

    @Test
    void ca19TextoYCodigoSonInversosParaLas32Acciones() {
        for (int a = 0; a < 32; a++) {
            assertEquals(a, Estado.accionDesde(Estado.accionComoTexto(a)),
                    "inversa para accion " + a);
        }
    }

    @Test
    void ca19FormatoTextoDeUnaAccion() {
        assertEquals("01+", Estado.accionComoTexto(Estado.accionDesde("01+")));
        assertEquals("33-", Estado.accionComoTexto(Estado.accionDesde("33-")));
        assertEquals(0, Estado.accionDesde("00-"), "00- es el codigo 0");
        assertEquals(16, Estado.accionDesde("00+"), "00+ es el codigo 16");
    }

    @Test
    void ca110TablaDeAccionesCoincideConElFormatoTexto() {
        assertEquals("00+", Estado.accionComoTexto(Estado.ACCIONES[0]));
        assertEquals("00-", Estado.accionComoTexto(Estado.ACCIONES[16]));
        assertEquals(32, Estado.NUM_ACCIONES);
    }

    // --- CA-1.11 -------------------------------------------------------------

    @Test
    void ca111EqualsYHashCodeDependenDelBitboard() {
        Estado a = resuelto();
        Estado b = resuelto();
        assertEquals(a, b);
        assertEquals(a, a);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(Long.hashCode(a.bitboard()), a.hashCode());
        assertNotEquals(a, a.aplicar(Estado.accionDesde("00+")));
        assertFalse(a.equals(null));
        assertFalse(a.equals(RESUELTO));
    }

    // --- CA-1.12 -------------------------------------------------------------

    @Test
    void ca112CadenaConLongitudIncorrecta() {
        assertThrows(IllegalArgumentException.class, () -> new Estado("00"));
        assertThrows(IllegalArgumentException.class, () -> new Estado(RESUELTO + "00"));
        assertThrows(IllegalArgumentException.class, () -> new Estado(""));
        assertThrows(IllegalArgumentException.class, () -> new Estado((String) null));
    }

    @Test
    void ca112CadenaConNoDigitos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Estado("0A010203040506070809101112131415"));
    }

    @Test
    void ca112FichaFueraDeRangoEnCadena() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Estado("00160203040506070809101112131415"));
        assertTrue(e.getMessage().contains("16"), "el mensaje incluye el valor: " + e.getMessage());
    }

    @Test
    void ca112FichaDuplicada() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Estado("00000203040506070809101112131415"));
        assertTrue(e.getMessage().contains("duplicada"), e.getMessage());
    }

    @Test
    void ca112ConstructorDeArrayInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Estado((int[]) null));
        assertThrows(IllegalArgumentException.class, () -> new Estado(new int[15]));
        assertThrows(IllegalArgumentException.class, () -> new Estado(new int[17]));
        assertThrows(IllegalArgumentException.class,
                () -> new Estado(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16}));
        assertThrows(IllegalArgumentException.class,
                () -> new Estado(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, -1}));
        assertThrows(IllegalArgumentException.class,
                () -> new Estado(new int[]{0, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}));
    }

    @Test
    void ca112ConstructorDeArrayValido() {
        assertEquals(resuelto(),
                new Estado(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}));
    }

    @Test
    void ca112FichaFueraDeRangoPorIndice() {
        Estado e = resuelto();
        assertThrows(IllegalArgumentException.class, () -> e.ficha(16));
        assertThrows(IllegalArgumentException.class, () -> e.ficha(-1));
        assertThrows(IllegalArgumentException.class, () -> e.ficha(4, 0));
    }

    @Test
    void ca112AplicarConAccionInvalida() {
        Estado e = resuelto();
        assertThrows(IllegalArgumentException.class, () -> e.aplicar(-1));
        assertThrows(IllegalArgumentException.class, () -> e.aplicar(32));
        assertThrows(IllegalArgumentException.class, () -> e.aplicar(999));
    }

    @Test
    void ca112AccionComoTextoInvalida() {
        assertThrows(IllegalArgumentException.class, () -> Estado.accionComoTexto(-1));
        assertThrows(IllegalArgumentException.class, () -> Estado.accionComoTexto(32));
    }

    @Test
    void ca112AccionDesdeInvalida() {
        assertThrows(IllegalArgumentException.class, () -> Estado.accionDesde(null));
        assertThrows(IllegalArgumentException.class, () -> Estado.accionDesde("0"));
        assertThrows(IllegalArgumentException.class, () -> Estado.accionDesde("01"));
        assertThrows(IllegalArgumentException.class, () -> Estado.accionDesde("011"));
        assertThrows(IllegalArgumentException.class, () -> Estado.accionDesde("0X+"));
        assertThrows(IllegalArgumentException.class, () -> Estado.accionDesde("40+"));
        assertThrows(IllegalArgumentException.class, () -> Estado.accionDesde("04+"));
        assertThrows(IllegalArgumentException.class, () -> Estado.accionDesde("01x"));
        assertThrows(IllegalArgumentException.class, () -> Estado.accionDesde("01*"));
    }

    // --- CA-1.13 -------------------------------------------------------------

    @Test
    void ca113AccionAplicadaPorCodigoYPorTextoDaLoMismo() {
        int accion = Estado.accionDesde("21+");
        Estado aplicada = resuelto().aplicar(accion);
        assertEquals(aplicada, resuelto().aplicar(Estado.accionDesde("21+")));
        assertNotEquals(resuelto(), aplicada);
    }

    @Test
    void ca113AplicarUnaSecuenciaEsDeterminista() {
        Estado a = resuelto().aplicar(Estado.accionDesde("01+")).aplicar(Estado.accionDesde("33-"));
        Estado b = resuelto().aplicar(Estado.accionDesde("12-")).aplicar(Estado.accionDesde("00+"));
        assertNotEquals(a, b, "secuencias distintas llevan a estados distintos");
        Estado a2 = resuelto().aplicar(Estado.accionDesde("01+")).aplicar(Estado.accionDesde("33-"));
        assertEquals(a, a2, "la misma secuencia es determinista");
    }
}
