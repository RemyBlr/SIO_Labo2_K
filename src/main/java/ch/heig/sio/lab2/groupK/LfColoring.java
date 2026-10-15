package ch.heig.sio.lab2.groupK;

import ch.heig.sio.lab2.coloring.Coloring;
import ch.heig.sio.lab2.coloring.ColoringAlgorithm;
import ch.heig.sio.lab2.graph.Graph;

/**
 * Coloration du plus grand degré en premier, donc dans l'ordre décroissant.<br>
 * Pas de départage des sommets s'il y a égalité.<br>
 * Complexité temporelle et spatiale de O(n + m) dans le pire des cas.
 */
public final class LfColoring implements ColoringAlgorithm {

  @Override
  public Coloring color(Graph graph) {
    int[] colors = new int[graph.vertexCount()];
    return new Coloring(colors);
  }

}
