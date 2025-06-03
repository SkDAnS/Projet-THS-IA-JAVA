package neurone;

import java.util.Random;

public class testNeurone
{
	final static float bruitAmplitude = 0.2f; // Amplitude du bruit blanc ajouté aux entrées

	/**
	 * Teste l'apprentissage d'un neurone sur la fonction ET.
	 * @param args Aucun argument n'est attendu.
	 */
	public static void main(final String[] args)
	{
		// On définit les entrées et les résultats de la fonction ET
		final float[][] entrees = new float[][] {
				new float[] { 0, 0 },
				new float[] { 0, 1 },
				new float[] { 1, 0 },
				new float[] { 1, 1 }
		};
		final float[] resultats = new float[] { 0, 0, 0, 1 };

		// On définit la limite de l'erreur quadratique moyenne
		final float MSElimite = 0.01f;

		// On crée un neurone avec le nombre d'entrées approprié

		final iNeurone n = new NeuroneHeavyside(entrees[0].length);
		//final iNeurone n = new NeuroneSigmoide(entrees[0].length);
		//final iNeurone n = new NeuroneReLU(entrees[0].length);

		System.out.println("Apprentissage…");
		// On lance l'apprentissage de la fonction ET sur ce neurone
		n.apprentissage(entrees, resultats, MSElimite);

		// On affiche les valeurs des synapses et du biais

		// Conversion dynamique d'une référence iNeurone vers une référence Neurone.
		final Neurone vueNeurone = (Neurone)n;
		System.out.print("Synapses : ");
		for (final float f : vueNeurone.synapses())
			System.out.print(f + " ");
		System.out.print("\nBiais : ");
		System.out.println(vueNeurone.biais());

		// On affiche chaque cas appris
		for (int i = 0; i < entrees.length; ++i)
		{
			// Pour une entrée donnée
			final float[] entree = entrees[i];
			final float[] entreeBruitée = ajouterBruit(entree, bruitAmplitude);
			// On met à jour la sortie du neurone
			n.metAJour(entreeBruitée);
			// On affiche cette sortie
			System.out.println("Entree " + i + " : " + n.sortie());
		}
	}

	// Méthode pour ajouter du bruit blanc à un vecteur d'entrée
	static float[] ajouterBruit(float[] entree, float amplitude)
	{
		Random rand = new Random();
		float[] bruitée = new float[entree.length];
		for (int i = 0; i < entree.length; i++)
		{
			// Génère un bruit aléatoire entre -amplitude et +amplitude
			float bruit = (rand.nextFloat() * 2 - 1) * amplitude;
			bruitée[i] = entree[i] + bruit;

			// On limite la valeur dans l'intervalle [0,1]
			if (bruitée[i] < 0)
				bruitée[i] = 0;
			if (bruitée[i] > 1)
				bruitée[i] = 1;
		}
		return bruitée;
	}




}