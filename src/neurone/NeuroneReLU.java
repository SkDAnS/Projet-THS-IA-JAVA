package neurone;

/**
 * Neurone avec fonction d'activation Leaky ReLU
 * Évite le problème du "neurone mort" en gardant une petite activation négative
 */
public class NeuroneReLU extends Neurone
{
    // Coefficient de fuite pour les valeurs négatives (1% de l'entrée passe)
    private static final float ALPHA = 0.01f;

    /**
     * Fonction d'activation Leaky ReLU : valeur si positive, sinon ALPHA * valeur
     * @param valeur somme pondérée des entrées
     * @return valeur si >= 0, sinon ALPHA * valeur
     */
    protected float activation(final float valeur) {
        return valeur >= 0 ? valeur : ALPHA * valeur;
    }

    /**
     * Dérivée de Leaky ReLU pour l'apprentissage par rétropropagation
     * @param valeur entrée de la fonction d'activation
     * @return 1.0f si valeur > 0, sinon ALPHA
     */
    protected float deriveeActivation(final float valeur) {
        return valeur > 0 ? 1.f : ALPHA;
    }

    /**
     * Constructeur : crée un neurone ReLU
     * @param nbEntrees nombre d'entrées du neurone
     */
    public NeuroneReLU (final int nbEntrees) {
        super(nbEntrees);
    }
}