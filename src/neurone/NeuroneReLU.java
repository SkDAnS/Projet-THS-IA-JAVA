package neurone;
public class NeuroneReLU extends Neurone
{
    private static final float ALPHA = 0.01f; // Paramètre pour Leaky ReLU

    // Leaky ReLU : évite que le neurone "meure"
    protected float activation(final float valeur) {
        return valeur >= 0 ? valeur : ALPHA * valeur;
    }

    // Dérivée de Leaky ReLU
    protected float deriveeActivation(final float valeur) {
        return valeur > 0 ? 1.f : ALPHA;
    }

    // Constructeur
    public NeuroneReLU (final int nbEntrees) {super(nbEntrees);}
}