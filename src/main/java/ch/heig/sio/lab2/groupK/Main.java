package ch.heig.sio.lab2.groupK;

import ch.heig.sio.lab2.coloring.Coloring;
import ch.heig.sio.lab2.coloring.ColoringAlgorithm;
import ch.heig.sio.lab2.graph.Graph;
import ch.heig.sio.lab2.graph.GraphReader;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class Main {

  private static final int WARMUP = 5;
  private static final int REPETITIONS = 20;

  public static void main(String[] args) throws IOException {
    Path dataDir = Path.of(args.length > 0 ? args[0] : "data");

    List<ColoringAlgorithm> algorithms = List.of(
        new TrivialColoring(),
        new LfColoring(),
        new DsaturColoring()
    );

    List<Path> files;
    try (Stream<Path> stream = Files.list(dataDir)) {
      files = stream.filter(p -> p.toString().endsWith(".txt")).sorted().toList();
    }

    for (Path file : files) {
      Graph graph;
      try {
        graph = GraphReader.read(file);
      } catch (IOException e) {
        System.err.println(file.getFileName() + " : " + e.getMessage());
        continue;
      }
      System.out.println(file.getFileName() + " " + graph);

      for(ColoringAlgorithm algorithm : algorithms) {
        double micros = averageTimeInMicros(algorithm, graph);
        Coloring coloring = algorithm.color(graph);

        System.out.printf("  %-16s %5d couleurs  %10.1f µs  %s%n",
                algorithm.name(), coloring.colorCount(), micros,
                coloring.isProperFor(graph) ? "OK" : "INVALIDE");
      }
    }
  }

  /**
   * Mesure le temps moyen d'un appel à {@code color(graph)}, après {@link #WARMUP}
   * exécutions d'échauffement, sur {@link #REPETITIONS} exécutions.
   *
   * @param algorithm l'algorithme de coloration
   * @param graph le graphe à colorer
   * @return le temps moyen en microsecondes pour colorer le graphe
   */
  private static double averageTimeInMicros(ColoringAlgorithm algorithm, Graph graph) {
    int colorSum = 0;
    for(int i = 0; i < WARMUP; ++i)
      colorSum += algorithm.color(graph).colorCount();

    long total = 0;
    for(int i = 0; i < REPETITIONS; ++i) {
      long start = System.nanoTime();
      Coloring coloring = algorithm.color(graph);
      total += System.nanoTime() - start;
      colorSum += coloring.colorCount();
    }
    // n'affiche rien si erreur
    if (colorSum < 0) System.out.print("");
    return (double) total / REPETITIONS / 1_000.0;
  }
}
