package com.martim.futcegosmanager

/**
 * Mercado.kt
 *
 * Mercado de transferências - calcula valor de mercado de cada
 * jogador, permite fazer ofertas entre times, e negocia
 * automaticamente (aceita se a oferta for boa o suficiente).
 */

data class OfertaTransferencia(
    val jogador: Jogador,
    val timeVendedor: Time,
    val timeComprador: Time,
    val valorOferta: Long,
)

enum class ResultadoNegociacao { ACEITA, RECUSADA_VALOR_BAIXO, RECUSADA_SEM_ORCAMENTO, RECUSADA_ULTIMO_DA_POSICAO }

data class RespostaNegociacao(val resultado: ResultadoNegociacao, val mensagem: String)

/** Valor de mercado cresce de forma NÃO linear com o overall - um
 * craque vale muito mais que a soma de vários jogadores medianos,
 * igual acontece no futebol de verdade. */
fun valorDeMercado(jogador: Jogador): Long {
    val overall = jogador.overall().toDouble()
    val base = Math.pow(overall / 10.0, 3.0) * 1000
    return base.toLong()
}

/** Confere se vender esse jogador deixaria o time sem ninguém pra
 * aquela posição (nunca pode ficar sem goleiro, por exemplo). */
private fun ficariaSemPosicao(time: Time, jogador: Jogador): Boolean {
    val quantosNaPosicao = time.elenco.count { it.posicao == jogador.posicao }
    val minimoNecessario = if (jogador.posicao == Posicao.GOLEIRO) 1 else 4
    return quantosNaPosicao <= minimoNecessario
}

fun negociarTransferencia(oferta: OfertaTransferencia): RespostaNegociacao {
    val valorMinimo = valorDeMercado(oferta.jogador)

    if (oferta.timeComprador.orcamento < oferta.valorOferta) {
        return RespostaNegociacao(
            ResultadoNegociacao.RECUSADA_SEM_ORCAMENTO,
            "${oferta.timeComprador.nome} não tem orçamento suficiente pra essa oferta.",
        )
    }

    if (ficariaSemPosicao(oferta.timeVendedor, oferta.jogador)) {
        return RespostaNegociacao(
            ResultadoNegociacao.RECUSADA_ULTIMO_DA_POSICAO,
            "${oferta.timeVendedor.nome} recusou: ficaria sem jogador suficiente nessa posição.",
        )
    }

    // Times de nível mais alto pedem um "extra" em cima do valor de mercado (não vendem barato os craques).
    val margemExigida = valorMinimo * 0.15 // pede pelo menos 15% a mais que o valor "justo"
    if (oferta.valorOferta < valorMinimo + margemExigida) {
        return RespostaNegociacao(
            ResultadoNegociacao.RECUSADA_VALOR_BAIXO,
            "${oferta.timeVendedor.nome} recusou a oferta de ${oferta.valorOferta} - queria pelo menos ${(valorMinimo + margemExigida).toLong()}.",
        )
    }

    // Negócio fechado - move o jogador e o dinheiro.
    oferta.timeVendedor.elenco.remove(oferta.jogador)
    oferta.timeComprador.elenco.add(oferta.jogador)
    oferta.timeVendedor.orcamento += oferta.valorOferta
    oferta.timeComprador.orcamento -= oferta.valorOferta

    return RespostaNegociacao(
        ResultadoNegociacao.ACEITA,
        "Negócio fechado! ${oferta.jogador.nome} vai do ${oferta.timeVendedor.nome} pro ${oferta.timeComprador.nome} por ${oferta.valorOferta}.",
    )
}

/** Lista os jogadores de um time ordenados por valor de mercado,
 * do mais caro pro mais barato - útil pra tela de "elenco à venda". */
fun elencoOrdenadoPorValor(time: Time): List<Pair<Jogador, Long>> {
    return time.elenco.map { it to valorDeMercado(it) }.sortedByDescending { it.second }
}
