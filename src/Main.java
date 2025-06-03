import FFT.Complexe;
import FFT.ComplexeCartesien;
import FFT.FFTCplx;
import neurone.NeuroneHeavyside;
import neurone.NeuroneSigmoide;
import neurone.NeuroneReLU;
import java.io.File;

public class Main {

    static final int tailleBloc = 1024;
    static final int nbExtraitsParClasse = 20;

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println("Usage : java Main miaulement.wav aboiement.wav fichier_long.wav [R|H|S]");
            System.out.println("  R = ReLU");
            System.out.println("  H = Heavyside");
            System.out.println("  S = Sigmoide");
            return;
        }

        String fichierMiaulement = args[0];
        String fichierAboiement = args[1];
        String fichierLong = args[2];
        String typeActivation = args[3].toUpperCase();

        if (!typeActivation.equals("R") && !typeActivation.equals("H") && !typeActivation.equals("S")) {
            System.out.println("Erreur : Type d'activation invalide. Utilisez R, H ou S");
            return;
        }

        String fichierNeurone;
        String typeNeurone;

        switch (typeActivation) {
            case "R":
                fichierNeurone = "neuroneChatChienReLU.txt";
                typeNeurone = "ReLU";
                break;
            case "H":
                fichierNeurone = "neuroneChatChienHeavyside.txt";
                typeNeurone = "Heavyside";
                break;
            case "S":
                fichierNeurone = "neuroneChatChienSigmoide.txt";
                typeNeurone = "Sigmoide";
                break;
            default:
                return;
        }

        System.out.println("Type de neurone sélectionné : " + typeNeurone);
        System.out.println("Fichier de sauvegarde : " + fichierNeurone);

        Object neurone = creerNeurone(typeActivation, tailleBloc / 2);
        if (neurone == null) {
            System.err.println("Erreur : Impossible de créer le neurone de type " + typeNeurone);
            return;
        }

        File fichier = new File(fichierNeurone);
        if (fichier.exists() && fichier.isFile()) {
            System.out.println("Fichier de neurone trouvé : " + fichierNeurone);
            System.out.println("Tentative de chargement du neurone existant...");

            boolean chargementReussi = chargerNeurone(neurone, typeActivation, fichierNeurone);

            if (chargementReussi) {
                System.out.println("Neurone chargé avec succès depuis : " + fichierNeurone);
            } else {
                System.out.println("Échec du chargement. Création et entraînement d'un nouveau neurone...");
                entrainerNeurone(neurone, typeActivation, fichierMiaulement, fichierAboiement, fichierNeurone);
            }
        } else {
            System.out.println("Fichier de neurone non trouvé : " + fichierNeurone);
            System.out.println("Création et entraînement d'un nouveau neurone " + typeNeurone + "...");
            entrainerNeurone(neurone, typeActivation, fichierMiaulement, fichierAboiement, fichierNeurone);
        }

        analyserFichierLong(neurone, typeActivation, fichierLong);
    }

    private static Object creerNeurone(String typeActivation, int taille) {
        try {
            switch (typeActivation) {
                case "R":
                    return new NeuroneReLU(taille);
                case "H":
                    return new NeuroneHeavyside(taille);
                case "S":
                    return new NeuroneSigmoide(taille);
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du neurone : " + e.getMessage());
            return null;
        }
    }

    private static boolean chargerNeurone(Object neurone, String typeActivation, String fichierNeurone) {
        try {
            switch (typeActivation) {
                case "R":
                    ((NeuroneReLU) neurone).chargement(fichierNeurone);
                    return true;
                case "H":
                    ((NeuroneHeavyside) neurone).chargement(fichierNeurone);
                    return true;
                case "S":
                    ((NeuroneSigmoide) neurone).chargement(fichierNeurone);
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement : " + e.getMessage());
            return false;
        }
    }

    private static void entrainerNeurone(Object neurone, String typeActivation, String fichierMiaulement,
                                         String fichierAboiement, String fichierNeurone) {
        System.out.println("Chargement des fichiers d'entraînement...");

        Son.Son sonChat = new Son.Son(fichierMiaulement);
        Son.Son sonChien = new Son.Son(fichierAboiement);

        float[][] entrees = new float[2 * nbExtraitsParClasse][];
        float[] sorties = new float[2 * nbExtraitsParClasse];

        System.out.println("Extraction des caractéristiques audio...");

        for (int i = 0; i < nbExtraitsParClasse; i++) {
            entrees[i] = blocVersSpectre(sonChat.bloc_deTaille(i, tailleBloc));
            sorties[i] = 1; // miaulement

            entrees[i + nbExtraitsParClasse] = blocVersSpectre(sonChien.bloc_deTaille(i, tailleBloc));
            sorties[i + nbExtraitsParClasse] = 0; // aboiement
        }

        System.out.println("Entraînement du neurone en cours...");

        try {

            switch (typeActivation) {
                case "R":
                    ((NeuroneReLU) neurone).apprentissage(entrees, sorties, 0.01f);
                    break;
                case "H":
                    ((NeuroneHeavyside) neurone).apprentissage(entrees, sorties, 0.01f);
                    break;
                case "S":
                    ((NeuroneSigmoide) neurone).apprentissage(entrees, sorties, 0.01f);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'entraînement : " + e.getMessage());
            return;
        }

        System.out.println("Sauvegarde du neurone entraîné...");
        try {
            switch (typeActivation) {
                case "R":
                    ((NeuroneReLU) neurone).sauvegarde(fichierNeurone);
                    break;
                case "H":
                    ((NeuroneHeavyside) neurone).sauvegarde(fichierNeurone);
                    break;
                case "S":
                    ((NeuroneSigmoide) neurone).sauvegarde(fichierNeurone);
                    break;
            }
            System.out.println("Neurone sauvegardé avec succès dans : " + fichierNeurone);
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    private static void analyserFichierLong(Object neurone, String typeActivation, String fichierLong) {
        System.out.println("\nAnalyse du fichier : " + fichierLong);

        Son.Son sonLong = new Son.Son(fichierLong);
        int nbBlocs = sonLong.taille() / tailleBloc;
        int nbChats = 0;
        int nbChiens = 0;


        System.out.println("Nombre de blocs à analyser : " + nbBlocs);
        System.out.println("\nRésultats de détection :");
        System.out.println("========================");

        for (int i = 0; i < nbBlocs; i++) {
            float[] bloc = sonLong.bloc_deTaille(i, tailleBloc);
            float[] spectre = blocVersSpectre(bloc);

            float sortie = 0;

            try {
                switch (typeActivation) {
                    case "R":
                        ((NeuroneReLU) neurone).metAJour(spectre);
                        sortie = ((NeuroneReLU) neurone).sortie();
                        break;
                    case "H":
                        ((NeuroneHeavyside) neurone).metAJour(spectre);
                        sortie = ((NeuroneHeavyside) neurone).sortie();
                        break;
                    case "S":
                        ((NeuroneSigmoide) neurone).metAJour(spectre);
                        sortie = ((NeuroneSigmoide) neurone).sortie();
                        break;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'analyse du bloc " + i + " : " + e.getMessage());
                continue;
            }


            float confiance;
            String resultat;

            if (typeActivation.equals("R")) {
                // Pour ReLU : classification binaire simple sans affichage de confiance en pourcentage
                if (sortie > 0.5f) {
                    resultat = "Chat";
                    nbChats++;
                } else {
                    resultat = "Chien";
                    nbChiens++;
                }
                // Pas d'affichage de confiance pour ReLU
                System.out.printf("Bloc %4d : %s (Sortie: %.3f)\n", i, resultat, sortie);
            } else {
                // pour Heavyside et Sigmoide, sortie déjà entre 0 et 1, on affiche la confiance en %
                if (sortie > 0.5f) {
                    confiance = sortie * 100;
                    resultat = "Chat";
                    nbChats++;
                } else {
                    confiance = (1 - sortie) * 100;
                    resultat = "Chien";
                    nbChiens++;
                }
                System.out.printf("Bloc %4d : %s (Sortie: %.3f, Confiance: %.1f%%)\n", i, resultat, sortie, confiance);
            }


        }

        System.out.println("\n========================");
        System.out.printf("Résumé de détection : %d Chats, %d Chiens\n", nbChats, nbChiens);

        if (nbChats + nbChiens > 0) {
            double pourcentageChats = (double) nbChats / (nbChats + nbChiens) * 100;
            double pourcentageChiens = (double) nbChiens / (nbChats + nbChiens) * 100;

            System.out.printf("Pourcentages : %.1f%% Chats, %.1f%% Chiens\n",
                    pourcentageChats, pourcentageChiens);
        }
    }

    static float[] blocVersSpectre(float[] bloc) {
        Complexe[] entree = new Complexe[bloc.length];
        for (int i = 0; i < bloc.length; i++) {
            entree[i] = new ComplexeCartesien(bloc[i], 0);
        }

        Complexe[] fft = FFTCplx.appliqueSur(entree);

        int tailleSpectre = bloc.length / 2;
        float[] spectre = new float[tailleSpectre];
        for (int i = 0; i < tailleSpectre; i++) {
            spectre[i] = (float) fft[i].mod();
        }
        float max = 0;
        for (float v : spectre) {
            if (v > max) max = v;
        }
        if (max > 0) {
            for (int i = 0; i < spectre.length; i++) {
                spectre[i] /= max;
            }
        }
        return spectre;
    }
}