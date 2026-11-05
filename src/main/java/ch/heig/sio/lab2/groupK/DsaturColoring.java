package ch.heig.sio.lab2.groupK;

import ch.heig.sio.lab2.coloring.Coloring;
import ch.heig.sio.lab2.coloring.ColoringAlgorithm;
import ch.heig.sio.lab2.graph.Graph;

/**
 * Coloration DSATUR de Brélaz : à chaque itération, on colore le sommet non colorié
 * de plus grand degré de saturation, avec la plus petite couleur non utilisée par ses
 * voisins déjà coloriés.<br>
 * En cas d'égalité, on prend le sommet de plus grand degré dans le graphe initial
 * (départage arbitraire en cas de double égalité).<br>
 * Complexité temporelle et spatiale de O(n²) dans le pire des cas.
 */
public final class DsaturColoring implements ColoringAlgorithm {

  /**
   * Initialise les structures de travail, puis colore un sommet par itération
   * (pseudocode, lignes 1 à 44).
   * <p>
   * Complexité : O(n²) en temps et en espace.
   *
   * @param graph le graphe à colorer
   * @return une coloration propre et complète du graphe
   */
  @Override
  public Coloring color(Graph graph) {
    int n = graph.vertexCount();

    // 1. Initialisation : saturation à 0 et forbiddenColors à false par défaut en Java
    int[] degrees = new int[n];
    int[] saturation = new int[n];
    int[] colors = new int[n];
    boolean[][] forbiddenColors = new boolean[n][n];

    for (int v = 0; v < n; v++) {
      degrees[v] = graph.degree(v);
      colors[v] = Coloring.UNCOLORED;
    }

    // 2. Boucle principale : un sommet colorié par itération
    for (int iteration = 0; iteration < n; iteration++) {
      int chosen = selectVertex(colors, saturation, degrees);
      int color = smallestAvailableColor(forbiddenColors[chosen]);
      colors[chosen] = color;
      updateNeighbors(graph, chosen, color, colors, forbiddenColors, saturation);
    }

    return new Coloring(colors);
  }

  /**
   * Choisit le prochain sommet à colorer : plus grand degré de saturation, puis plus
   * grand degré initial, puis premier rencontré (pseudocode, lignes 13 à 27).
   * <p>
   * Complexité : O(n).
   *
   * @param colors     couleur de chaque sommet ({@link Coloring#UNCOLORED} si non colorié)
   * @param saturation degré de saturation courant de chaque sommet
   * @param degrees    degré initial de chaque sommet
   * @return le numéro du sommet choisi
   */
  private static int selectVertex(int[] colors, int[] saturation, int[] degrees) {
    int chosen = -1;
    for (int sommet = 0; sommet < colors.length; sommet++) {
      if (colors[sommet] == Coloring.UNCOLORED
              && (chosen == -1
              || saturation[sommet] > saturation[chosen]
              || (saturation[sommet] == saturation[chosen] && degrees[sommet] > degrees[chosen]))) {
        chosen = sommet;
      }
    }
    return chosen;
  }

  /**
   * Cherche la plus petite couleur disponible pour un sommet (pseudocode, lignes 28 à 32).
   * <p>
   * Complexité : O(n).
   *
   * @param forbiddenColors ligne de {@code couleur_interdite} du sommet :
   *                        {@code forbiddenColors[c]} est vrai si un voisin colorié a la couleur {@code c}
   * @return la plus petite couleur non interdite
   */
  private static int smallestAvailableColor(boolean[] forbiddenColors) {
    // Au plus n-1 couleurs interdites sur n : la boucle s'arrête toujours dans les bornes
    int candidate = 0;
    while (forbiddenColors[candidate]) {
      candidate++;
    }
    return candidate;
  }

  /**
   * Met à jour les couleurs interdites et les degrés de saturation des voisins non
   * coloriés du sommet qui vient d'être coloré (pseudocode, lignes 34 à 41).
   * <p>
   * Complexité : O(deg(vertex)), soit O(m) au total sur toute l'exécution.
   *
   * @param graph           le graphe à colorer
   * @param vertex          le sommet qui vient d'être coloré
   * @param color           la couleur attribuée à {@code vertex}
   * @param colors          couleur de chaque sommet ({@link Coloring#UNCOLORED} si non colorié)
   * @param forbiddenColors tableau {@code couleur_interdite} de taille n × n
   * @param saturation      degré de saturation courant de chaque sommet
   */
  private static void updateNeighbors(Graph graph, int vertex, int color, int[] colors,
                                      boolean[][] forbiddenColors, int[] saturation) {
    int degree = graph.degree(vertex);
    for (int i = 0; i < degree; i++) {
      int neighbor = graph.neighbor(vertex, i);
      // La saturation compte les couleurs différentes : seule une couleur nouvelle l'augmente
      if (colors[neighbor] == Coloring.UNCOLORED && !forbiddenColors[neighbor][color]) {
        forbiddenColors[neighbor][color] = true;
        saturation[neighbor]++;
      }
    }
  }
}
