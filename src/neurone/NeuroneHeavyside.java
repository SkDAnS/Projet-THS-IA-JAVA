package neurone;

/**
 * Neurone avec fonction d'activation Heavyside (binaire : 0 ou 1)
 * Utilisé pour des classifications strictes sans nuances
 */
public class NeuroneHeavyside extends Neurone
{
	/**
	 * Fonction d'activation binaire : renvoie 1 si positif, 0 si négatif
	 * @param valeur somme pondérée des entrées
	 * @return 1.0f si valeur >= 0, sinon 0.0f
	 */
	protected float activation(final float valeur) {
		return valeur >= 0 ? 1.f : 0.f;
	}

	/**
	 * Constructeur : crée un neurone Heavyside
	 * @param nbEntrees nombre d'entrées du neurone
	 */
	public NeuroneHeavyside(final int nbEntrees) {
		super(nbEntrees);
	}
}