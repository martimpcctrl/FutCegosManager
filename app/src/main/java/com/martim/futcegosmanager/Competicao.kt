package com.martim.futcegosmanager

import kotlin.random.Random

/**
 * Competicao.kt
 *
 * Monta o Campeonato Brasileiro de Futebol de Cegos - Série A 2025,
 * com os 12 times reais (pesquisei a tabela oficial, incluindo os
 * resultados de verdade das quartas/semis/final antes de montar a
 * força de cada time).
 *
 * Grupos reais:
 *   Grupo A: AGAFUC-RS, ADESUL-CE, UNIACE-DF, INSEP-SP
 *   Grupo B: Corinthians-SP, INV-SP, APADV-SP, CEDEMAC-MA
 *   Grupo C: APACE-PB, APADEVI-PB, AMC-MT, Vila Nova-GO
 *
 * Nível de força (1 a 5) baseado no desempenho real no mata-mata 2025:
 *   5 - AGAFUC (campeão), Corinthians (vice, perdeu só nos pênaltis)
 *   4 - APACE (bronze), AMC-MT (4º lugar, bateu o ADESUL nas quartas)
 *   3 - INV-SP, APADV-SP, ADESUL-CE (perderam nas quartas, mas por
 *       margem pequena/competitiva)
 *   2 - APADEVI-PB (perdeu nas quartas por margem grande, 4x0)
 *   1 - UNIACE-DF, INSEP-SP, CEDEMAC-MA, Vila Nova-GO (não passaram
 *       da fase de grupos)
 *
 * Jogadores REAIS confirmados pela pesquisa (alguns da Seleção
 * Brasileira, medalhista de bronze em Paris 2024): Ricardinho e
 * Nonato (AGAFUC), Tiago Paraná, Cássio e Jefinho (Corinthians),
 * Paulinho (APACE), goleiros Luan (AGAFUC) e Giovanni (Corinthians).
 * O resto de cada elenco é fictício.
 */

data class DadosTime(val nome: String, val sigla: String, val grupo: String, val nivel: Int)

val TIMES_SERIE_A_2025 = listOf(
    DadosTime("AGAFUC", "RS", "A", 5),
    DadosTime("ADESUL", "CE", "A", 3),
    DadosTime("UNIACE", "DF", "A", 1),
    DadosTime("INSEP", "SP", "A", 1),
    DadosTime("Corinthians", "SP", "B", 5),
    DadosTime("INV", "SP", "B", 3),
    DadosTime("APADV", "SP", "B", 3),
    DadosTime("CEDEMAC", "MA", "B", 1),
    DadosTime("APACE", "PB", "C", 4),
    DadosTime("APADEVI", "PB", "C", 2),
    DadosTime("AMC", "MT", "C", 4),
    DadosTime("Vila Nova", "GO", "C", 1),
)

data class JogadoresReaisTime(val goleiro: String?, val linhaSelecao: List<String>, val linhaTime: List<String>)

val JOGADORES_REAIS: Map<String, JogadoresReaisTime> = mapOf(
    "AGAFUC" to JogadoresReaisTime(goleiro = "Luan", linhaSelecao = listOf("Ricardinho", "Nonato"), linhaTime = listOf("Bill", "Léo")),
    "Corinthians" to JogadoresReaisTime(goleiro = "Giovanni", linhaSelecao = listOf("Tiago Paraná", "Cássio", "Jefinho"), linhaTime = emptyList()),
    "APACE" to JogadoresReaisTime(goleiro = null, linhaSelecao = emptyList(), linhaTime = listOf("Paulinho")),
)

private val NOMES_LINHA_GENERICOS = listOf(
    "Bruno", "Carlos", "Diego", "Eduardo", "Felipe", "Gabriel", "Henrique",
    "Igor", "João", "Kaio", "Lucas", "Marcos", "Nathan", "Otávio", "Pedro",
    "Rafael", "Samuel", "Thiago", "Vinícius", "William",
)
private val NOMES_GOLEIROS_GENERICOS = listOf("André", "Bernardo", "Caio", "Danilo", "Emerson", "Fábio")

private fun faixaPorNivel(nivel: Int): IntRange = when (nivel) {
    5 -> 70..94
    4 -> 62..88
    3 -> 55..82
    2 -> 48..76
    else -> 40..70
}

fun gerarTime(dados: DadosTime, seed: Long? = null): Time {
    val rng = if (seed != null) Random(seed) else Random.Default
    val time = Time(nome = dados.nome, sigla = dados.sigla)
    val faixa = faixaPorNivel(dados.nivel)
    val dadosReais = JOGADORES_REAIS[dados.nome] ?: JogadoresReaisTime(null, emptyList(), emptyList())

    val nomeGoleiro = dadosReais.goleiro ?: NOMES_GOLEIROS_GENERICOS.random(rng)
    time.elenco.add(
        Jogador(
            nomeGoleiro, Posicao.GOLEIRO,
            reflexo = rng.nextInt(faixa.first, faixa.last + 1),
            defesa = rng.nextInt(faixa.first - 5, faixa.last - 5 + 1),
            fisico = rng.nextInt(faixa.first - 10, faixa.last - 10 + 1),
        )
    )

    for (nomeCraque in dadosReais.linhaSelecao) {
        time.elenco.add(
            Jogador(nomeCraque, Posicao.LINHA, ataque = rng.nextInt(88, 97), defesa = rng.nextInt(75, 89), fisico = rng.nextInt(80, 93))
        )
    }
    for (nomeReal in dadosReais.linhaTime) {
        time.elenco.add(
            Jogador(nomeReal, Posicao.LINHA, ataque = rng.nextInt(faixa.first, faixa.last + 1), defesa = rng.nextInt(faixa.first - 5, faixa.last - 5 + 1), fisico = rng.nextInt(faixa.first, faixa.last + 1))
        )
    }

    val faltando = maxOf(0, 6 - dadosReais.linhaSelecao.size - dadosReais.linhaTime.size)
    val nomesDisponiveis = NOMES_LINHA_GENERICOS.shuffled(rng).take(faltando)
    for (nome in nomesDisponiveis) {
        time.elenco.add(
            Jogador(nome, Posicao.LINHA, ataque = rng.nextInt(faixa.first - 10, faixa.last - 10 + 1), defesa = rng.nextInt(faixa.first - 10, faixa.last - 10 + 1), fisico = rng.nextInt(faixa.first - 5, faixa.last - 5 + 1))
        )
    }

    return time
}

fun montarCampeonatoSerieA(seed: Long? = null): List<Time> {
    return TIMES_SERIE_A_2025.mapIndexed { indice, dados ->
        val seedTime = seed?.plus(indice)
        gerarTime(dados, seedTime)
    }
}

fun timesDoGrupo(times: List<Time>, grupo: String): List<Time> {
    val nomesDoGrupo = TIMES_SERIE_A_2025.filter { it.grupo == grupo }.map { it.nome }
    return times.filter { it.nome in nomesDoGrupo }
}

fun gerarConfrontosDentroDoGrupo(times: List<Time>, grupo: String): List<Pair<Time, Time>> {
    val doGrupo = timesDoGrupo(times, grupo)
    val confrontos = mutableListOf<Pair<Time, Time>>()
    for (i in doGrupo.indices) {
        for (j in i + 1 until doGrupo.size) {
            confrontos.add(doGrupo[i] to doGrupo[j])
        }
    }
    return confrontos
}

fun jogarFaseDeGrupos(times: List<Time>, seed: Long? = null): List<ResultadoPartida> {
    val rng = if (seed != null) Random(seed) else Random.Default
    val resultados = mutableListOf<ResultadoPartida>()
    for (grupo in listOf("A", "B", "C")) {
        for ((mandante, visitante) in gerarConfrontosDentroDoGrupo(times, grupo)) {
            resultados.add(simularPartida(mandante, visitante, seed = rng.nextLong()))
        }
    }
    return resultados
}

fun tabelaDoGrupo(times: List<Time>, grupo: String): List<Time> {
    return timesDoGrupo(times, grupo).sortedWith(
        compareByDescending<Time> { it.pontos }.thenByDescending { it.vitorias }.thenByDescending { it.saldoGols() }.thenByDescending { it.golsPro }
    )
}

fun artilharia(times: List<Time>, top: Int = 10): List<Jogador> {
    return times.flatMap { it.elenco }.sortedByDescending { it.golsNaTemporada }.take(top)
}
