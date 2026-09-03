package com.martim.futcegosmanager

import org.junit.Assert.*
import org.junit.Test

class MotorTest {

    private fun criarTimeForte(): Time {
        val time = Time("Time Forte", "FOR")
        time.elenco.add(Jogador("Goleiro Forte", Posicao.GOLEIRO, reflexo = 90, defesa = 80))
        for (i in 1..4) {
            time.elenco.add(Jogador("Atacante Forte $i", Posicao.LINHA, ataque = 90, defesa = 70, fisico = 80))
        }
        return time
    }

    private fun criarTimeFraco(): Time {
        val time = Time("Time Fraco", "FRA")
        time.elenco.add(Jogador("Goleiro Fraco", Posicao.GOLEIRO, reflexo = 30, defesa = 30))
        for (i in 1..4) {
            time.elenco.add(Jogador("Atacante Fraco $i", Posicao.LINHA, ataque = 25, defesa = 25, fisico = 30))
        }
        return time
    }

    @Test
    fun `overall do goleiro usa reflexo com mais peso`() {
        val goleiro = Jogador("Teste", Posicao.GOLEIRO, reflexo = 80, defesa = 60, fisico = 50)
        assertTrue(goleiro.overall() > 60)
    }

    @Test
    fun `overall do jogador de linha usa ataque com mais peso`() {
        val atacante = Jogador("Teste", Posicao.LINHA, ataque = 80, defesa = 40, fisico = 60)
        assertTrue(atacante.overall() > 50)
    }

    @Test
    fun `escalacao titular tem exatamente 5 jogadores`() {
        val time = criarTimeForte()
        time.elenco.add(Jogador("Reserva", Posicao.LINHA, ataque = 10))
        assertEquals(5, time.escalacaoTitular().size)
    }

    @Test
    fun `escalacao titular escolhe o melhor goleiro`() {
        val time = Time("Teste", "TST")
        time.elenco.add(Jogador("Goleiro Ruim", Posicao.GOLEIRO, reflexo = 40))
        time.elenco.add(Jogador("Goleiro Bom", Posicao.GOLEIRO, reflexo = 90))
        for (i in 1..4) time.elenco.add(Jogador("Linha $i", Posicao.LINHA, ataque = 50))
        val titulares = time.escalacaoTitular()
        assertTrue(titulares.any { it.nome == "Goleiro Bom" })
        assertFalse(titulares.any { it.nome == "Goleiro Ruim" })
    }

    @Test
    fun `time forte tem forca geral maior que time fraco`() {
        assertTrue(criarTimeForte().forcaGeral() > criarTimeFraco().forcaGeral())
    }

    @Test
    fun `time sem elenco tem forca zero e nao trava`() {
        val vazio = Time("Vazio", "VAZ")
        assertEquals(0.0, vazio.forcaGeral(), 0.001)
    }

    @Test
    fun `time forte vence a maioria das partidas contra time fraco`() {
        var vitoriasForte = 0
        for (i in 0 until 30) {
            val forte = criarTimeForte()
            val fraco = criarTimeFraco()
            val resultado = simularPartida(forte, fraco, seed = i.toLong())
            if (resultado.vencedor() === forte) vitoriasForte++
        }
        assertTrue("Time forte venceu $vitoriasForte/30 vezes", vitoriasForte > 15)
    }

    @Test
    fun `mesma seed da o mesmo placar`() {
        val a1 = simularPartida(criarTimeForte(), criarTimeFraco(), seed = 999L)
        val a2 = simularPartida(criarTimeForte(), criarTimeFraco(), seed = 999L)
        assertEquals(a1.golsMandante, a2.golsMandante)
        assertEquals(a1.golsVisitante, a2.golsVisitante)
    }

    @Test
    fun `eventos ficam em ordem crescente de minuto`() {
        val resultado = simularPartida(criarTimeForte(), criarTimeFraco(), seed = 42L)
        for (i in 0 until resultado.eventos.size - 1) {
            assertTrue(resultado.eventos[i].minuto <= resultado.eventos[i + 1].minuto)
        }
    }

    @Test
    fun `quantidade de eventos de gol bate com o placar`() {
        val resultado = simularPartida(criarTimeForte(), criarTimeFraco(), seed = 42L)
        val golsNosEventos = resultado.eventos.count { it.tipo == "gol" }
        assertEquals(resultado.golsMandante + resultado.golsVisitante, golsNosEventos)
    }

    @Test
    fun `vencedor ganha 3 pontos`() {
        val forte = criarTimeForte()
        val fraco = criarTimeFraco()
        val pontosAntes = forte.pontos
        val resultado = simularPartida(forte, fraco, seed = 7L)
        if (resultado.vencedor() === forte) {
            assertEquals(pontosAntes + 3, forte.pontos)
        }
    }

    @Test
    fun `saldo de gols calculado certo`() {
        val time = Time("Saldo", "SAL", golsPro = 10, golsContra = 4)
        assertEquals(6, time.saldoGols())
    }
}
