package neurone;

/**
 * Neurone avec fonction d'activation sigmoïde
 * Sortie entre 0 et 1, idéale pour les probabilités
 */
public class NeuroneSigmoide extends Neurone
{
    /**
     * Fonction d'activation sigmoïde : 1 / (1 + e^(-x))
     * Transforme toute valeur en nombre entre 0 et 1
     * @param valeur somme pondérée des entrées
     * @return valeur entre 0.0f et 1.0f
     */
    protected float activation(final float valeur) {
        return (float)(1.0 / (1.0 + Math.exp(-valeur)));
    }

    /**
     * Constructeur : crée un neurone sigmoïde
     * @param nbEntrees nombre d'entrées du neurone
     */
    public NeuroneSigmoide(final int nbEntrees) {
        super(nbEntrees);
    }
}