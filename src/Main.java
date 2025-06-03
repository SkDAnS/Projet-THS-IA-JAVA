// Importation des classes nécessaires pour la FFT (transformée de Fourier rapide)
import FFT.Complexe;
import FFT.ComplexeCartesien;
import FFT.FFTCplx;
// Importation des différents types de neurones artificiels
import neurone.NeuroneHeavyside;
import neurone.NeuroneSigmoide;
import neurone.NeuroneReLU;
import java.io.File;

/**
 * Programme principal de classification audio : Chat vs Chien
 *
 * Ce programme utilise l'intelligence artificielle (réseaux de neurones) pour distinguer
 * automatiquement les miaulements de chat des aboiements de chien dans des fichiers audio.
 *
 * Principe de fonctionnement :
 * 1. Analyse spectrale : conversion du son en fréquences (comme un égaliseur audio)
 * 2. Apprentissage : le neurone apprend à reconnaître les différences entre chat et chien
 * 3. Classification : analyse d'un long fichier audio pour détecter chaque animal
 */
public class Main {

    // CONSTANTES DU PROGRAMME
    // Taille de chaque segment audio analysé (1024 échantillons ≈ 0.02 seconde à 44kHz)
    static final int tailleBloc = 1024;
    // Nombre d'exemples utilisés pour entraîner le neurone (20 miaulements + 20 aboiements)
    static final int nbExtraitsParClasse = 20;

    /**
     * Méthode principale du programme
     * @param args Arguments de la ligne de commande :
     *            [0] fichier de miaulements
     *            [1] fichier d'aboiements
     *            [2] fichier long à analyser
     *            [3] type de neurone (R/H/S)
     */
    public static void main(String[] args) {

        // VÉRIFICATION DES ARGUMENTS
        // Le programme a besoin de exactement 4 paramètres pour fonctionner
        if (args.length != 4) {
            System.out.println("Usage : java Main miaulement.wav aboiement.wav fichier_long.wav [R|H|S]");
            System.out.println("  R = ReLU");        // Fonction d'activation linéaire rectifiée
            System.out.println("  H = Heavyside");   // Fonction d'activation en escalier (0 ou 1)
            System.out.println("  S = Sigmoide");    // Fonction d'activation en courbe S (0 à 1)
            return;
        }

        // RÉCUPÉRATION DES PARAMÈTRES
        String fichierMiaulement = args[0];  // Fichier d'exemples de miaulements
        String fichierAboiement = args[1];   // Fichier d'exemples d'aboiements
        String fichierLong = args[2];        // Fichier long à analyser
        String typeActivation = args[3].toUpperCase(); // Type de neurone choisi

        // VALIDATION DU TYPE DE NEURONE
        // Seuls R, H et S sont acceptés comme types de neurones
        if (!typeActivation.equals("R") && !typeActivation.equals("H") && !typeActivation.equals("S")) {
            System.out.println("Erreur : Type d'activation invalide. Utilisez R, H ou S");
            return;
        }

        // CONFIGURATION DU NEURONE SELON LE TYPE CHOISI
        String fichierNeurone;  // Nom du fichier de sauvegarde du neurone entraîné
        String typeNeurone;     // Nom complet du type de neurone

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

        // CRÉATION DU NEURONE
        // Création d'un neurone avec une taille d'entrée de 512 (tailleBloc/2 fréquences)
        Object neurone = creerNeurone(typeActivation, tailleBloc / 2);
        if (neurone == null) {
            System.err.println("Erreur : Impossible de créer le neurone de type " + typeNeurone);
            return;
        }

        // VÉRIFICATION DE L'EXISTENCE D'UN NEURONE DÉJÀ ENTRAÎNÉ
        // Si un neurone a déjà été entraîné et sauvegardé, on essaie de le charger
        File fichier = new File(fichierNeurone);
        if (fichier.exists() && fichier.isFile()) {
            System.out.println("Fichier de neurone trouvé : " + fichierNeurone);
            System.out.println("Tentative de chargement du neurone existant...");

            // Tentative de chargement du neurone pré-entraîné
            boolean chargementReussi = chargerNeurone(neurone, typeActivation, fichierNeurone);

            if (chargementReussi) {
                System.out.println("Neurone chargé avec succès depuis : " + fichierNeurone);
            } else {
                // Si le chargement échoue, on crée et entraîne un nouveau neurone
                System.out.println("Échec du chargement. Création et entraînement d'un nouveau neurone...");
                entrainerNeurone(neurone, typeActivation, fichierMiaulement, fichierAboiement, fichierNeurone);
            }
        } else {
            // Aucun neurone pré-entraîné trouvé, création d'un nouveau neurone
            System.out.println("Fichier de neurone non trouvé : " + fichierNeurone);
            System.out.println("Création et entraînement d'un nouveau neurone " + typeNeurone + "...");
            entrainerNeurone(neurone, typeActivation, fichierMiaulement, fichierAboiement, fichierNeurone);
        }

        // ANALYSE DU FICHIER LONG
        // Utilisation du neurone (entraîné ou chargé) pour analyser le fichier long
        analyserFichierLong(neurone, typeActivation, fichierLong);
    }

    /**
     * Crée une instance de neurone selon le type spécifié
     * @param typeActivation Type de fonction d'activation (R/H/S)
     * @param taille Nombre d'entrées du neurone (nombre de fréquences analysées)
     * @return Instance du neurone créé, ou null en cas d'erreur
     */
    private static Object creerNeurone(String typeActivation, int taille) {
        try {
            switch (typeActivation) {
                case "R":
                    // ReLU : f(x) = max(0, x) - Bonne pour la classification
                    return new NeuroneReLU(taille);
                case "H":
                    // Heavyside : f(x) = 0 si x<0, 1 si x≥0 - Classification binaire stricte
                    return new NeuroneHeavyside(taille);
                case "S":
                    // Sigmoide : f(x) = 1/(1+e^-x) - Sortie probabiliste entre 0 et 1
                    return new NeuroneSigmoide(taille);
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du neurone : " + e.getMessage());
            return null;
        }
    }

    /**
     * Charge un neurone pré-entraîné depuis un fichier
     * @param neurone Instance du neurone à charger
     * @param typeActivation Type du neurone
     * @param fichierNeurone Chemin vers le fichier de sauvegarde
     * @return true si le chargement réussit, false sinon
     */
    private static boolean chargerNeurone(Object neurone, String typeActivation, String fichierNeurone) {
        try {
            switch (typeActivation) {
                case "R":
                    // Cast de l'objet générique vers le type spécifique et chargement
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

    /**
     * Entraîne le neurone avec des exemples de miaulements et d'aboiements
     * @param neurone Instance du neurone à entraîner
     * @param typeActivation Type du neurone
     * @param fichierMiaulement Fichier contenant des exemples de miaulements
     * @param fichierAboiement Fichier contenant des exemples d'aboiements
     * @param fichierNeurone Fichier où sauvegarder le neurone entraîné
     */
    private static void entrainerNeurone(Object neurone, String typeActivation, String fichierMiaulement,
                                         String fichierAboiement, String fichierNeurone) {
        System.out.println("Chargement des fichiers d'entraînement...");

        // CHARGEMENT DES FICHIERS AUDIO D'ENTRAÎNEMENT
        Son.Son sonChat = new Son.Son(fichierMiaulement);   // Fichier des miaulements
        Son.Son sonChien = new Son.Son(fichierAboiement);   // Fichier des aboiements

        // PRÉPARATION DES DONNÉES D'ENTRAÎNEMENT
        // Création des tableaux pour stocker les données d'entrée et les résultats attendus
        float[][] entrees = new float[2 * nbExtraitsParClasse][];  // 40 exemples au total
        float[] sorties = new float[2 * nbExtraitsParClasse];      // Résultats attendus

        System.out.println("Extraction des caractéristiques audio...");

        // EXTRACTION DES CARACTÉRISTIQUES AUDIO
        for (int i = 0; i < nbExtraitsParClasse; i++) {
            // Extraction et conversion en spectre de fréquences pour les miaulements
            entrees[i] = blocVersSpectre(sonChat.bloc_deTaille(i, tailleBloc));
            sorties[i] = 1; // miaulement = 1 (classe positive)

            // Extraction et conversion en spectre de fréquences pour les aboiements
            entrees[i + nbExtraitsParClasse] = blocVersSpectre(sonChien.bloc_deTaille(i, tailleBloc));
            sorties[i + nbExtraitsParClasse] = 0; // aboiement = 0 (classe négative)
        }

        System.out.println("Entraînement du neurone en cours...");

        try {
            // PHASE D'APPRENTISSAGE
            // Le neurone ajuste ses poids synaptiques pour apprendre à distinguer chat/chien
            switch (typeActivation) {
                case "R":
                    // Taux d'apprentissage = 0.01 (vitesse d'adaptation du neurone)
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

        // SAUVEGARDE DU NEURONE ENTRAÎNÉ
        System.out.println("Sauvegarde du neurone entraîné...");
        try {
            switch (typeActivation) {
                case "R":
                    // Sauvegarde des poids et paramètres du neurone dans un fichier
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

    /**
     * Analyse un fichier audio long pour détecter la présence de chats et de chiens
     * @param neurone Neurone entraîné à utiliser pour la classification
     * @param typeActivation Type du neurone
     * @param fichierLong Chemin vers le fichier audio à analyser
     */
    private static void analyserFichierLong(Object neurone, String typeActivation, String fichierLong) {
        System.out.println("\nAnalyse du fichier : " + fichierLong);

        // CHARGEMENT DU FICHIER AUDIO À ANALYSER
        Son.Son sonLong = new Son.Son(fichierLong);
        int nbBlocs = sonLong.taille() / tailleBloc;  // Nombre de segments à analyser
        int nbChats = 0;   // Compteur de détections de chats
        int nbChiens = 0;  // Compteur de détections de chiens

        System.out.println("Nombre de blocs à analyser : " + nbBlocs);
        System.out.println("\nRésultats de détection :");
        System.out.println("========================");

        // ANALYSE BLOC PAR BLOC
        // Chaque bloc représente environ 0.02 seconde d'audio
        for (int i = 0; i < nbBlocs; i++) {
            // Extraction d'un segment audio
            float[] bloc = sonLong.bloc_deTaille(i, tailleBloc);
            // Conversion en spectre de fréquences (comme pour l'entraînement)
            float[] spectre = blocVersSpectre(bloc);

            float sortie = 0;  // Résultat de la classification

            try {
                // CLASSIFICATION DU SEGMENT AUDIO
                switch (typeActivation) {
                    case "R":
                        // Mise à jour du neurone avec les nouvelles données
                        ((NeuroneReLU) neurone).metAJour(spectre);
                        // Récupération de la sortie du neurone
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

            // INTERPRÉTATION DU RÉSULTAT
            float confiance;
            String resultat;

            if (typeActivation.equals("R")) {
                // Pour ReLU : classification binaire simple sans affichage de confiance en pourcentage
                // Seuil de décision à 0.5
                if (sortie > 0.5f) {
                    resultat = "Chat";
                    nbChats++;
                } else {
                    resultat = "Chien";
                    nbChiens++;
                }
                // Pas d'affichage de confiance pour ReLU (sortie peut être > 1)
                System.out.printf("Bloc %4d : %s (Sortie: %.3f)\n", i, resultat, sortie);
            } else {
                // Pour Heavyside et Sigmoide, sortie déjà entre 0 et 1, on affiche la confiance en %
                if (sortie > 0.5f) {
                    // Plus la sortie est proche de 1, plus on est confiant que c'est un chat
                    confiance = sortie * 100;
                    resultat = "Chat";
                    nbChats++;
                } else {
                    // Plus la sortie est proche de 0, plus on est confiant que c'est un chien
                    confiance = (1 - sortie) * 100;
                    resultat = "Chien";
                    nbChiens++;
                }
                System.out.printf("Bloc %4d : %s (Sortie: %.3f, Confiance: %.1f%%)\n", i, resultat, sortie, confiance);
            }
        }

        // AFFICHAGE DU RÉSUMÉ FINAL
        System.out.println("\n========================");
        System.out.printf("Résumé de détection : %d Chats, %d Chiens\n", nbChats, nbChiens);

        // Calcul et affichage des pourcentages
        if (nbChats + nbChiens > 0) {
            double pourcentageChats = (double) nbChats / (nbChats + nbChiens) * 100;
            double pourcentageChiens = (double) nbChiens / (nbChats + nbChiens) * 100;

            System.out.printf("Pourcentages : %.1f%% Chats, %.1f%% Chiens\n",
                    pourcentageChats, pourcentageChiens);
        }
    }

    /**
     * Convertit un bloc audio en spectre de fréquences normalisé
     *
     * Cette fonction est cruciale car elle transforme le signal audio temporel
     * en information fréquentielle que le neurone peut analyser.
     *
     * @param bloc Échantillons audio bruts
     * @return Spectre de fréquences normalisé (amplitudes entre 0 et 1)
     */
    static float[] blocVersSpectre(float[] bloc) {
        // PRÉPARATION POUR LA FFT (Transformée de Fourier Rapide)
        // Conversion des échantillons réels en nombres complexes
        Complexe[] entree = new Complexe[bloc.length];
        for (int i = 0; i < bloc.length; i++) {
            // Partie réelle = échantillon audio, partie imaginaire = 0
            entree[i] = new ComplexeCartesien(bloc[i], 0);
        }

        // APPLICATION DE LA FFT
        // Transformation du signal temporel en signal fréquentiel
        // Révèle quelles fréquences sont présentes dans le son
        Complexe[] fft = FFTCplx.appliqueSur(entree);

        // EXTRACTION DES AMPLITUDES DES FRÉQUENCES
        // On ne garde que la moitié du spectre (l'autre moitié est symétrique)
        int tailleSpectre = bloc.length / 2;
        float[] spectre = new float[tailleSpectre];
        for (int i = 0; i < tailleSpectre; i++) {
            // Calcul de l'amplitude (module du nombre complexe)
            spectre[i] = (float) fft[i].mod();
        }

        // NORMALISATION DU SPECTRE
        // Mise à l'échelle entre 0 et 1 pour faciliter l'apprentissage du neurone
        float max = 0;
        // Recherche de la valeur maximale
        for (float v : spectre) {
            if (v > max) max = v;
        }
        // Division par le maximum si celui-ci est non nul
        if (max > 0) {
            for (int i = 0; i < spectre.length; i++) {
                spectre[i] /= max;
            }
        }
        return spectre;
    }
}