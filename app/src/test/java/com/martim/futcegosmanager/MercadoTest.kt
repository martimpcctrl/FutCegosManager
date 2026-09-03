package com.martim.futcegosmanager

import org.junit.Assert.*
import org.junit.Test

class MercadoTest {

    private fun timeComElencoCompleto(nome: String, orcamento: Long = 100_000L): Time {
        val time = Time(nome, nome.take(3).uppercase(), orcamento = orcamento)
        time.elenco.add(Jogador("Goleiro", Posicao.GOLEIRO, reflexo = 50))
        for (i in 1..5) time.elenco.add(Jogador("Linha $i", Posicao.LINHA, ataque = 50))
        return time
    }

    @Test
    fun `craque vale muito mais que jogador mediano`() {
        val craque = Jogador("Craque", Posicao.LINHA, ataque = 95, defesa = 85, fisico = 90)
        val mediano = Jogador("Mediano", Posicao.LINHA, ataque = 50, defesa = 50, fisico = 50)
        assertTrue(valorDeMercado(craque) > valorDeMercado(mediano) * 5)
    }

    @Test
    fun `oferta boa e aceita e move o jogador`() {
        val comprador = Time("Comprador", "COM", orcamento = 10_000_000L)
        val vendedor = timeComElencoCompleto("Vendedor")
        val jogador = Jogador("Jogador X", Posicao.LINHA, ataque = 60, defesa = 60, fisico = 60)
        vendedor.elenco.add(jogador)

        val oferta = OfertaTransferencia(jogador, vendedor, comprador, (valorDeMercado(jogador) * 1.3).toLong())
        val resposta = negociarTransferencia(oferta)

        assertEquals(ResultadoNegociacao.ACEITA, resposta.resultado)
        assertTrue(comprador.elenco.contains(jogador))
        assertFalse(vendedor.elenco.contains(jogador))
    }

    @Test
    fun `oferta muito baixa e recusada`() {
        val comprador = Time("Comprador", "COM", orcamento = 10_000_000L)
        val vendedor = timeComElencoCompleto("Vendedor")
        val jogador = Jogador("Jogador Y", Posicao.LINHA, ataque = 70)
        vendedor.elenco.add(jogador)

        val oferta = OfertaTransferencia(jogador, vendedor, comprador, 100L)
        val resposta = negociarTransferencia(oferta)

        assertEquals(ResultadoNegociacao.RECUSADA_VALOR_BAIXO, resposta.resultado)
        assertTrue(vendedor.elenco.contains(jogador))
    }

    @Test
    fun `comprador sem orcamento e recusado`() {
        val compradorPobre = Time("Pobre", "POB", orcamento = 10L)
        val vendedor = timeComElencoCompleto("Vendedor")
        val jogador = Jogador("Jogador Z", Posicao.LINHA, ataque = 60)
        vendedor.elenco.add(jogador)

        val oferta = OfertaTransferencia(jogador, vendedor, compradorPobre, valorDeMercado(jogador) * 2)
        val resposta = negociarTransferencia(oferta)

        assertEquals(ResultadoNegociacao.RECUSADA_SEM_ORCAMENTO, resposta.resultado)
    }

    @Test
    fun `nao deixa vender o unico goleiro do time`() {
        val comprador = Time("Comprador", "COM", orcamento = 10_000_000L)
        val vendedor = Time("Vendedor", "VEN", orcamento = 100_000L)
        val unicoGoleiro = Jogador("Único Goleiro", Posicao.GOLEIRO, reflexo = 80)
        vendedor.elenco.add(unicoGoleiro)
        for (i in 1..5) vendedor.elenco.add(Jogador("Linha $i", Posicao.LINHA, ataque = 40))

        val oferta = OfertaTransferencia(unicoGoleiro, vendedor, comprador, valorDeMercado(unicoGoleiro) * 2)
        val resposta = negociarTransferencia(oferta)

        assertEquals(ResultadoNegociacao.RECUSADA_ULTIMO_DA_POSICAO, resposta.resultado)
    }

    @Test
    fun `elenco ordenado por valor traz o mais caro primeiro`() {
        val time = Time("Teste", "TST")
        time.elenco.add(Jogador("Fraco", Posicao.LINHA, ataque = 30, defesa = 30, fisico = 30))
        time.elenco.add(Jogador("Forte", Posicao.LINHA, ataque = 90, defesa = 90, fisico = 90))
        val ordenado = elencoOrdenadoPorValor(time)
        assertEquals("Forte", ordenado.first().first.nome)
    }
}
