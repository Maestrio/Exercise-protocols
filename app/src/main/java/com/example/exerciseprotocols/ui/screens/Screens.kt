package com.example.exerciseprotocols.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.exerciseprotocols.viewmodel.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProtocolListScreen(viewModel: ListViewModel, onAdd: () -> Unit, onOpen: (Long) -> Unit, onEdit: (Long) -> Unit) {
    val items by viewModel.protocols.collectAsStateWithLifecycle()
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { protocol ->
                Card(Modifier.fillMaxWidth().combinedClickable(onClick = { onOpen(protocol.id) }, onLongClick = { onEdit(protocol.id) })) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(protocol.name, style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { viewModel.delete(protocol) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}

@Composable
fun ProtocolBuilderScreen(vm: BuilderViewModel, onBack: () -> Unit) {
    val name by vm.name.collectAsStateWithLifecycle()
    val rest by vm.restBetweenExercises.collectAsStateWithLifecycle()
    val ex by vm.exercises.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(name, { vm.name.value = it }, label = { Text("Protocol name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(rest, { vm.restBetweenExercises.value = it }, label = { Text("Rest between exercises (sec)") }, modifier = Modifier.fillMaxWidth())
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(ex) { i, item ->
                Card {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Exercise ${i + 1}", fontWeight = FontWeight.Bold)
                        OutlinedTextField(item.name, { item.name = it; vm.exercises.value = ex.toList() }, label = { Text("Name") })
                        OutlinedTextField(item.work, { item.work = it; vm.exercises.value = ex.toList() }, label = { Text("Work sec") })
                        OutlinedTextField(item.sets, { item.sets = it; vm.exercises.value = ex.toList() }, label = { Text("Sets") })
                        OutlinedTextField(item.rest, { item.rest = it; vm.exercises.value = ex.toList() }, label = { Text("Rest sec") })
                        Row { 
                            IconButton(onClick = { if (i > 0) vm.exercises.value = ex.toMutableList().apply { add(i - 1, removeAt(i)) } }) { Icon(Icons.Default.ArrowUpward, null) }
                            IconButton(onClick = { if (i < ex.lastIndex) vm.exercises.value = ex.toMutableList().apply { add(i + 1, removeAt(i)) } }) { Icon(Icons.Default.ArrowDownward, null) }
                            TextButton(onClick = { vm.exercises.value = ex.toMutableList().apply { removeAt(i) } }) { Text("Delete") }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.exercises.value = ex + ExerciseInput() }) { Text("Add Exercise") }
            Button(onClick = { vm.save(onBack) }) { Text("Save") }
        }
    }
}

@Composable
fun ProtocolPlayerScreen(vm: PlayerViewModel, onBack: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LocalView.current.keepScreenOn = ui.running
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
        Text(ui.exercise, style = MaterialTheme.typography.headlineMedium)
        Text(ui.setLabel)
        Text(ui.phase.name, style = MaterialTheme.typography.titleLarge)
        Text("${ui.remaining}", style = MaterialTheme.typography.displayLarge)
        Text("Up next: ${ui.next}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::start) { Text("Start") }
            Button(onClick = vm::pause) { Text("Pause") }
            Button(onClick = vm::skip) { Text("Skip") }
            OutlinedButton(onClick = vm::reset) { Text("Reset") }
        }
        TextButton(onClick = onBack) { Text("Back") }
    }
}
