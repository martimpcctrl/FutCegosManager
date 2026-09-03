package com.martim.futcegosmanager

import org.junit.Assert.*
import org.junit.Test

class CompeticaoTest {

    @Test
    fun `campeonato tem os 12 times reais`() {
        val times = montarCampeonatoSerieA(seed = 1L)
        assertEquals(12, times.size)
    }

    @Test
    fun `times reais confirmados presentes`() {
        val times = montarCampeonatoSerieA(seed = 1L)
        val nomes = times.map { it.nome }
        for (nomeEsperado in listOf("AGAFUC", "Corinthians", "APACE", "APADEVI", "INV", "ADESUL", "UNIACE", "INSEP", "APADV", "CEDEMAC", "AMC", "Vila Nova")) {
            assertTrue("$nomeEsperado deveria estar na lista", nomeEsperado in nomes)
        }
    }

    @Test
    fun `Ricardinho e Nonato estao no AGAFUC`() {
        val times = montarCampeonatoSerieA(seed = 2L)
        val agafuc = times.first { it.nome == "AGAFUC" }
        val nomes = agafuc.elenco.map { it.nome }
        assertTrue("Ricardinho" in nomes)
        assertTrue("Nonato" in nomes)
        assertTrue("Luan" in nomes) // goleiro
    }

    @Test
    fun `craques da selecao tem ataque bem alto`() {
        val times = montarCampeonatoSerieA(seed = 3L)
        val agafuc = times.first { it.nome == "AGAFUC" }
        val ricardinho = agafuc.elenco.first { it.nome == "Ricardinho" }
        assertTrue(ricardinho.ataque >= 85)
    }

    @Test
    fun `Tiago Parana Cassio e Jefinho estao no Corinthians`() {
        val times = montarCampeonatoSerieA(seed = 4L)
        val corinthians = times.first { it.nome == "Corinthians" }
        val nomes = corinthians.elenco.map { it.nome }
        assertTrue("Tiago Paraná" in nomes)
        assertTrue("Cássio" in nomes)
        assertTrue("Jefinho" in nomes)
        assertTrue("Giovanni" in nomes) // goleiro
    }

    @Test
    fun `hierarquia real de forca - AGAFUC mais forte que CEDEMAC na maioria das vezes`() {
        var agafucMaisForte = 0
        for (s in 0 until 20) {
            val times = montarCampeonatoSerieA(seed = s.toLong() * 13)
            val agafuc = times.first { it.nome == "AGAFUC" }
            val cedemac = times.first { it.nome == "CEDEMAC" }
            if (agafuc.forcaGeral() > cedemac.forcaGeral()) agafucMaisForte++
        }
        assertTrue("AGAFUC só foi mais forte $agafucMaisForte/20 vezes", agafucMaisForte >= 18)
    }

    @Test
    fun `grupo A tem os 4 times certos`() {
        val times = montarCampeonatoSerieA(seed = 5L)
        val grupoA = timesDoGrupo(times, "A")
        val nomes = grupoA.map { it.nome }.toSet()
        assertEquals(setOf("AGAFUC", "ADESUL", "UNIACE", "INSEP"), nomes)
    }

    @Test
    fun `grupo de 4 times gera 6 confrontos`() {
        val times = montarCampeonatoSerieA(seed = 6L)
        val confrontos = gerarConfrontosDentroDoGrupo(times, "A")
        assertEquals(6, confrontos.size)
    }

    @Test
    fun `fase de grupos gera 18 partidas no total`() {
        val times = montarCampeonatoSerieA(seed = 7L)
        val resultados = jogarFaseDeGrupos(times, seed = 700L)
        assertEquals(18, resultados.size)
    }

    @Test
    fun `tabela do grupo fica ordenada por pontos`() {
        val times = montarCampeonatoSerieA(seed = 8L)
        jogarFaseDeGrupos(times, seed = 800L)
        val tabela = tabelaDoGrupo(times, "A")
        assertEquals(4, tabela.size)
        for (i in 0 until tabela.size - 1) {
            assertTrue(tabela[i].pontos >= tabela[i + 1].pontos)
        }
    }

    @Test
    fun `artilharia devolve o numero pedido de jogadores`() {
        val times = montarCampeonatoSerieA(seed = 9L)
        jogarFaseDeGrupos(times, seed = 900L)
        val lista = artilharia(times, top = 5)
        assertTrue(lista.size <= 5)
    }
}
