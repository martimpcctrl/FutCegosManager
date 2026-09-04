package com.martim.futcegosmanager

import kotlin.random.Random

/**
 * Motor.kt
 *
 * Motor do jogo de gerenciamento de futebol de cegos - código 100%
 * original, traduzido da versão Python já testada (47 testes
 * passando lá) pra Kotlin. Não usa nem se baseia em nenhum código
 * decompilado de terceiros.
 */

enum class Posicao { GOLEIRO, LINHA }

data class Jogador(
    val nome: String,
    val posicao: Posicao,
    var ataque: Int = 50,
    var defesa: Int = 50,
    var fisico: Int = 50,
    var reflexo: Int = 50, // só relevante pro goleiro
    var golsNaTemporada: Int = 0,
) {
    fun overall(): Int = if (posicao == Posicao.GOLEIRO) {
        Math.round(reflexo * 0.6 + defesa * 0.3 + fisico * 0.1).toInt()
    } else {
        Math.round(ataque * 0.4 + defesa * 0.3 + fisico * 0.3).toInt()
    }
}

data class Time(
    val nome: String,
    val sigla: String,
    val elenco: MutableList<Jogador> = mutableListOf(),
    var pontos: Int = 0,
    var vitorias: Int = 0,
    var empates: Int = 0,
    var derrotas: Int = 0,
    var golsPro: Int = 0,
    var golsContra: Int = 0,
    var orcamento: Long = 500_000L, // em reais fictícios, pra comprar/vender jogadores
) {
    /** Escala automaticamente: o goleiro de melhor overall, e os 4 de
     * linha de melhor overall - formato oficial do futebol de cegos
     * (1 goleiro + 4 de linha). */
    fun escalacaoTitular(): List<Jogador> {
        val goleiros = elenco.filter { it.posicao == Posicao.GOLEIRO }.sortedByDescending { it.overall() }
        val linha = elenco.filter { it.posicao == Posicao.LINHA }.sortedByDescending { it.overall() }
        val titulares = mutableListOf<Jogador>()
        goleiros.firstOrNull()?.let { titulares.add(it) }
        titulares.addAll(linha.take(4))
        return titulares
    }

    fun forcaGeral(): Double {
        val titulares = escalacaoTitular()
        if (titulares.isEmpty()) return 0.0
        return titulares.sumOf { it.overall() }.toDouble() / titulares.size
    }

    fun saldoGols(): Int = golsPro - golsContra
}

data class EventoPartida(
    val minuto: Int,
    val tipo: String, // "gol" | "chance_perdida" | "defesa"
    val time: String,
    val jogador: String?,
    val descricao: String,
)

data class ResultadoPartida(
    val mandante: Time,
    val visitante: Time,
    var golsMandante: Int = 0,
    var golsVisitante: Int = 0,
    val eventos: MutableList<EventoPartida> = mutableListOf(),
) {
    fun vencedor(): Time? = when {
        golsMandante > golsVisitante -> mandante
        golsVisitante > golsMandante -> visitante
        else -> null
    }
}

const val DURACAO_TEMPO_MINUTOS = 20 // 2 tempos de 20 minutos, aproximado

enum class EstiloDeJogo { EQUILIBRADO, ATAQUE_TOTAL, CONTRA_ATAQUE }
enum class IntensidadeMarcacao { LEVE, PESADA, MUITO_PESADA }
enum class FocoDeAtaque { PELO_MEIO, PELAS_LATERAIS }

/** Configuração tática do time - quem bate falta/escanteio, quem é
 * capitão, estilo de jogo. Separado do Time em si porque nem toda
 * tela precisa disso (só a tela de táticas). */
data class Taticas(
    var titularesEscolhidosManualmente: MutableList<Jogador>? = null, // null = usa a escalação automática
    var batedorFaltas: Jogador? = null,
    var capitao: Jogador? = null,
    var batedorEscanteios: Jogador? = null,
    var estiloDeJogo: EstiloDeJogo = EstiloDeJogo.EQUILIBRADO,
    var marcacao: IntensidadeMarcacao = IntensidadeMarcacao.PESADA,
    var focoDeAtaque: FocoDeAtaque = FocoDeAtaque.PELO_MEIO,
)

/** Devolve os titulares considerando a escolha manual do técnico, se
 * houver - senão cai pra escalação automática de sempre. */
fun titularesEfetivos(time: Time, taticas: Taticas): List<Jogador> {
    return taticas.titularesEscolhidosManualmente?.takeIf { it.isNotEmpty() } ?: time.escalacaoTitular()
}

/** Lógica pura de troca entre titular e disponível na tela de
 * escalação - separada da UI de propósito, pra dar pra testar sem
 * precisar do Compose. Devolve o novo par (titulares, disponíveis)
 * depois da troca. Só troca entre lados diferentes (um titular por um
 * disponível) - trocar dois do mesmo lado não faz sentido e não
 * altera nada. */
fun trocarJogadorNaEscalacao(
    titulares: List<Jogador>,
    disponiveis: List<Jogador>,
    jogadorQueSai: Jogador,
    jogadorQueEntra: Jogador,
): Pair<List<Jogador>, List<Jogador>> {
    val jogadorQueSaiEstaNosTitulares = titulares.contains(jogadorQueSai)
    val jogadorQueEntraEstaNosTitulares = titulares.contains(jogadorQueEntra)

    if (jogadorQueSaiEstaNosTitulares == jogadorQueEntraEstaNosTitulares) {
        return titulares to disponiveis
    }

    val novosTitulares = titulares.toMutableList()
    val novosDisponiveis = disponiveis.toMutableList()

    if (jogadorQueSaiEstaNosTitulares) {
        novosTitulares.remove(jogadorQueSai); novosTitulares.add(jogadorQueEntra)
        novosDisponiveis.remove(jogadorQueEntra); novosDisponiveis.add(jogadorQueSai)
    } else {
        novosTitulares.remove(jogadorQueEntra); novosTitulares.add(jogadorQueSai)
        novosDisponiveis.remove(jogadorQueSai); novosDisponiveis.add(jogadorQueEntra)
    }

    return novosTitulares to novosDisponiveis
}

private fun forcaParaProbabilidade(forcaTime: Double, forcaAdversario: Double): Double {
    val diferenca = forcaTime - forcaAdversario
    val base = 0.5 + (diferenca / 200)
    return base.coerceIn(0.08, 0.92)
}

fun simularPartida(mandante: Time, visitante: Time, seed: Long? = null): ResultadoPartida {
    val rng = if (seed != null) Random(seed) else Random.Default
    val resultado = ResultadoPartida(mandante = mandante, visitante = visitante)

    val forcaMandante = mandante.forcaGeral() + 3 // pequena vantagem de jogar em casa
    val forcaVisitante = visitante.forcaGeral()

    val titularesMandante = mandante.escalacaoTitular()
    val titularesVisitante = visitante.escalacaoTitular()
    val atacantesMandante = titularesMandante.filter { it.posicao == Posicao.LINHA }
    val atacantesVisitante = titularesVisitante.filter { it.posicao == Posicao.LINHA }
    val goleiroMandante = titularesMandante.firstOrNull { it.posicao == Posicao.GOLEIRO }
    val goleiroVisitante = titularesVisitante.firstOrNull { it.posicao == Posicao.GOLEIRO }

    val minutosTotais = DURACAO_TEMPO_MINUTOS * 2
    val totalChances = rng.nextInt(8, 17) // 8 a 16 chances na partida inteira
    val minutosDasChances = (1..minutosTotais).shuffled(rng).take(totalChances).sorted()

    for (minuto in minutosDasChances) {
        val probMandante = forcaParaProbabilidade(forcaMandante, forcaVisitante)
        val timeDaChance = if (rng.nextDouble() < probMandante) mandante else visitante

        val (atacantes, goleiroAdversario, nomeTime) = if (timeDaChance === mandante) {
            Triple(atacantesMandante, goleiroVisitante, mandante.nome)
        } else {
            Triple(atacantesVisitante, goleiroMandante, visitante.nome)
        }

        if (atacantes.isEmpty()) continue

        // Escolhe o atacante com peso pelo ataque (mais ataque = mais chance de ser ele na jogada).
        val pesos = atacantes.map { maxOf(1, it.ataque) }
        val somaPesos = pesos.sum()
        var sorteio = rng.nextInt(somaPesos)
        var atacante = atacantes.first()
        for (i in atacantes.indices) {
            if (sorteio < pesos[i]) {
                atacante = atacantes[i]
                break
            }
            sorteio -= pesos[i]
        }

        val forcaAtaque = atacante.ataque
        val forcaDefesa = goleiroAdversario?.reflexo ?: 40
        val chanceDeGol = forcaParaProbabilidade(forcaAtaque.toDouble(), forcaDefesa.toDouble())

        if (rng.nextDouble() < chanceDeGol) {
            if (timeDaChance === mandante) resultado.golsMandante++ else resultado.golsVisitante++
            atacante.golsNaTemporada++
            resultado.eventos.add(
                EventoPartida(minuto, "gol", nomeTime, atacante.nome, "GOL! ${atacante.nome} balança a rede pro $nomeTime!")
            )
        } else if (goleiroAdversario != null) {
            resultado.eventos.add(
                EventoPartida(minuto, "defesa", nomeTime, goleiroAdversario.nome, "Defesa de ${goleiroAdversario.nome}! ${atacante.nome} não teve sorte dessa vez.")
            )
        } else {
            resultado.eventos.add(
                EventoPartida(minuto, "chance_perdida", nomeTime, atacante.nome, "${atacante.nome} teve a chance, mas a bola não entrou.")
            )
        }
    }

    resultado.eventos.sortBy { it.minuto }

    mandante.golsPro += resultado.golsMandante
    mandante.golsContra += resultado.golsVisitante
    visitante.golsPro += resultado.golsVisitante
    visitante.golsContra += resultado.golsMandante

    when (resultado.vencedor()) {
        mandante -> {
            mandante.vitorias++; mandante.pontos += 3; visitante.derrotas++
        }
        visitante -> {
            visitante.vitorias++; visitante.pontos += 3; mandante.derrotas++
        }
        else -> {
            mandante.empates++; visitante.empates++
            mandante.pontos += 1; visitante.pontos += 1
        }
    }

    return resultado
}
