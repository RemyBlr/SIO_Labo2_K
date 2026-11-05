package ch.heig.sio.lab2.groupK;

import ch.heig.sio.lab2.coloring.Coloring;
import ch.heig.sio.lab2.coloring.ColoringAlgorithm;
import ch.heig.sio.lab2.graph.Graph;

import java.util.Arrays;

/**
 * Coloration du plus grand degré en premier, donc dans l'ordre décroissant.<br>
 * Pas de départage des sommets s'il y a égalité.<br>
 * Complexité temporelle et spatiale de O(n + m) dans le pire des cas.
 */
public final class LfColoring implements ColoringAlgorithm {

  @Override
  public Coloring color(Graph graph) {
    int n = graph.vertexCount();
    int maxDegree = graph.maxDegree();

    // 1. Calculer degré de chaque sommet et compter sommets par degré
    // O(n)
    int[] degrees = new int[n];
    int[] count = new int[maxDegree + 1];
    for (int v = 0; v < n; ++v) {
      degrees[v] = graph.degree(v);
      count[degrees[v]]++;
    }

    // 2. Classer par ordre décroissant de degré, tri par dénombrement
    // O(n)
    int[] start = new int[maxDegree + 1];
    for (int d = maxDegree - 1; d >= 0; --d)
      start[d] = start[d + 1] + count[d + 1];

    int[] order = new int[n];
    for (int v = 0; v < n; ++v)
      order[start[degrees[v]]++] = v;

    // 3. Parcourir la liste
    // O(n + m)
    int[] colors = new int[n];
    Arrays.fill(colors, Coloring.UNCOLORED);
    // forbidden[c] == v quand la couleur c est utilisée par un voisin de v
    int[] forbidden = new int[maxDegree + 1];
    Arrays.fill(forbidden, -1);

    for (int v : order) {
      int degree = degrees[v];

      // 3.1 Marquer couleurs des voisins déjà colorés (seulement <= degré utiles)
      for (int i = 0; i < degree; ++i) {
        int c = colors[graph.neighbor(v, i)];
        if (c != Coloring.UNCOLORED && c <= degree)
          forbidden[c] = v;
      }

      // 3.2 Colorer sommet courant avec plus petite couleur disponible
      // O(degré)
      int c = 0;
      while (forbidden[c] == v)
        c++;

      colors[v] = c;
    }

    return new Coloring(colors);
  }
}
