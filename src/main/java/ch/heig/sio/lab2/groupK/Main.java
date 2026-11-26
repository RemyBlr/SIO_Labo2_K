package ch.heig.sio.lab2.groupK;

import ch.heig.sio.lab2.coloring.Coloring;
import ch.heig.sio.lab2.coloring.ColoringAlgorithm;
import ch.heig.sio.lab2.graph.Graph;
import ch.heig.sio.lab2.graph.GraphReader;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class Main {

  // Nbr d'exécutions avant la mesure du temps
  private static final int WARMUP = 5;
  // Nbr de répétitions d'exécution, on en fait la moyenne
  private static final int REPETITIONS = 20;
  // Pour éviter que le compilateur optimise le code et élimines les mesures si non utilisées
  private static volatile int totalColors;

  public static void main(String[] args) throws IOException {
    Path dataDir = Path.of(args.length > 0 ? args[0] : "data");

    ColoringAlgorithm lf = new LfColoring();
    ColoringAlgorithm dsatur = new DsaturColoring();

    List<ColoringAlgorithm> algorithms = List.of(
        // pas besoin pour l'analyse
        // new TrivialColoring(),
        lf,
        dsatur
    );

    // lignes du tableau
    List<String> rows = new ArrayList<>();

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

      Map<ColoringAlgorithm, Integer> colorCounts = new HashMap<>();
      Map<ColoringAlgorithm, Double> times = new HashMap<>();

      for(ColoringAlgorithm algorithm : algorithms) {
        double micros = averageTimeInMicros(algorithm, graph);
        // Séparer la coloration du temps pour l'analyse
        Coloring coloring = algorithm.color(graph);
        colorCounts.put(algorithm, coloring.colorCount());
        times.put(algorithm, micros);

        System.out.printf("  %-16s %5d couleurs  %10.1f µs  %s%n",
                algorithm.name(), coloring.colorCount(), micros,
                coloring.isProperFor(graph) ? "OK" : "INVALIDE");
      }

      // Format du nom de fichier
      String fileName = file.getFileName().toString();
      String instance = fileName.substring(0, fileName.length() - ".txt".length());
      // Ajoute la ligne du tableau pour cette instance
      rows.add(tableRow(instance, graph,
              colorCounts.get(lf), colorCounts.get(dsatur),
              times.get(lf), times.get(dsatur)));
    }

    System.out.println();
    System.out.printf("Temps: moyenne sur %d répétitions après %d exécutions d'échauffement%n%n", REPETITIONS, WARMUP);
    System.out.println("| Instance | n | m | Δ | D | Couleurs LF | Couleurs DSATUR | Gain DSATUR sur LF | Temps LF en µs | Temps DSATUR en µs | Augmentation entre DSATUR et LF |");
    System.out.println("|---|--:|--:|--:|--:|--:|--:|--:|--:|--:|--:|");
    rows.forEach(System.out::println);
  }

  /**
   * Construit une ligne du tableau au format markdown pour chaque instance
   *
   * @param instance nom de l'instance
   * @param graph le graphe de l'instance
   * @param lfColors nombre de couleurs utilisées par LF
   * @param dsaturColors nombre de couleurs utilisées par DSATUR
   * @param lfMicros temps moyen de LF, en µs
   * @param dsaturMicros temps moyen de DSATUR, en µs
   * @return la ligne du tableau, au format markdown
   */
  private static String tableRow(String instance,
                                 Graph graph,
                                 int lfColors,
                                 int dsaturColors,
                                 double lfMicros,
                                 double dsaturMicros) {

    int n = graph.vertexCount();
    long m = graph.edgeCount();
    // Densité D du graphe
    double density = n == 1 ? 0.0 : (2.0 * m) / (double) (n * (n - 1));
    // Gain relatif des couleurs de DSATUR sur LF
    // lf - dsatur, car dsatur est plus performant, donc le gain est positif si DSATUR est meilleur
    double colorGain = 100.0 * (lfColors - dsaturColors) / lfColors;
    // Augmentation relative du temps d'éxec entre DSATUR et LF
    // dsatur - lf, car lf est plus rapide, donc l'augmentation est positive si DSATUR est plus lent
    double timeIncrease = 100.0 * (dsaturMicros - lfMicros) / lfMicros;

    return String.format("| %s | %d | %d | %d | %.3f | %d | %d | %.1f %% | %.1f | %.1f | %.1f %% |",
            instance, n, m, graph.maxDegree(), density, lfColors, dsaturColors, colorGain,
            lfMicros, dsaturMicros, timeIncrease);
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

    totalColors = colorSum;
    if (totalColors < 0) System.out.println("");

    return (double) total / REPETITIONS / 1_000.0;
  }
}
