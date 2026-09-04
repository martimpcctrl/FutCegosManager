package com.martim.futcegosmanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * TelaEscalacao.kt
 *
 * Tela de escalação - duas colunas (titulares / disponíveis), toca
 * num jogador de cada lado pra trocar de posição. Código original.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEscalacao(time: Time, taticas: Taticas, onVoltar: () -> Unit, onConfirmar: (Taticas) -> Unit) {
    var titulares by remember {
        mutableStateOf((taticas.titularesEscolhidosManualmente ?: time.escalacaoTitular()).toMutableList())
    }
    var disponiveis by remember {
        mutableStateOf(time.elenco.filter { it !in titulares }.toMutableList())
    }
    var selecionadoParaTroca by remember { mutableStateOf<Jogador?>(null) }

    fun trocar(jogadorClicado: Jogador, veioDosTitulares: Boolean) {
        val outroSelecionado = selecionadoParaTroca
        if (outroSelecionado == null) {
            selecionadoParaTroca = jogadorClicado
            return
        }
        if (outroSelecionado == jogadorClicado) {
            selecionadoParaTroca = null
            return
        }

        val (novosTitulares, novosDisponiveis) = trocarJogadorNaEscalacao(titulares, disponiveis, outroSelecionado, jogadorClicado)
        titulares = novosTitulares.toMutableList()
        disponiveis = novosDisponiveis.toMutableList()
        selecionadoParaTroca = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escalação") },
                navigationIcon = {
                    IconButton(onClick = onVoltar, modifier = Modifier.semantics { contentDescription = "Voltar" }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    taticas.titularesEscolhidosManualmente = titulares
                    onConfirmar(taticas)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).semantics { contentDescription = "Confirmar escalação e ir pro jogo" },
            ) { Text("Ir pro jogo") }
        },
    ) { paddingInterno ->
        Row(modifier = Modifier.fillMaxSize().padding(paddingInterno)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Escalados (${titulares.size})",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(12.dp),
                )
                LazyColumn {
                    items(titulares) { jogador ->
                        ItemJogadorEscalacao(
                            jogador = jogador,
                            selecionado = jogador == selecionadoParaTroca,
                            onClick = { trocar(jogador, veioDosTitulares = true) },
                        )
                    }
                }
            }
            VerticalDivider()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Disponíveis (${disponiveis.size})",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(12.dp),
                )
                LazyColumn {
                    items(disponiveis) { jogador ->
                        ItemJogadorEscalacao(
                            jogador = jogador,
                            selecionado = jogador == selecionadoParaTroca,
                            onClick = { trocar(jogador, veioDosTitulares = false) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemJogadorEscalacao(jogador: Jogador, selecionado: Boolean, onClick: () -> Unit) {
    val descricao = "${jogador.nome}, overall ${jogador.overall()}" + if (selecionado) ", selecionado pra troca" else ""
    val corDeFundo = if (selecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Surface(color = corDeFundo, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).semantics { contentDescription = descricao }) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(jogador.nome, style = MaterialTheme.typography.bodyMedium)
            Text("${jogador.overall()} overall", style = MaterialTheme.typography.bodySmall)
        }
    }
    HorizontalDivider()
}
