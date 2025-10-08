# SaveTheGoverment
Εδώ θα αναπτυξουμε την εργασια μας στο μαθημα ΠΡΟΓΡΑΜΜΑΤΙΣΜΟΣ ΙΙ

## Συντονισμός
- **Συναντηση** (διαδικτυακή ή φυσική) μια φορά την εβδομάδα,οπου θα αναλύουμε πρόοδο και τα επομενά μας βήματα
- **Εξοικείωση με το github** δειτε κανα βιντεακι στο youtube πχ https://www.youtube.com/watch?v=a9u2yZvsqHA ωστε να καταλάβετε πως λειτουργεί και τι προσφέρει.Να ξέρετε σίγουρα τι είναι :Repository,Branch,Pull Request(PR),Commit,Merge
- **Εξοικείωση με το git** Yπάρχουν σημειώσεις για το git στο moodle στις σημειώσεις φροντιστηρίου (φροντιστήριο 2).Θα μας βοηθήσει να χρησιμοποιούμε το github απο το command line επιταχύνοντας την ανάπτυξη του κώδικα.

## Οταν αρχίσουμε να γράφουμε κώδικα
- αυτό το αρχείο που διαβάζετε θα περιγράφει το πως λειτουργεί η εφαρμογη
- Θα φτιάξω ενα αλλο αρχείο (εκτος αν το αναλάβει κάποιος αλλος) ίδιου τύπου οπου θα περιγράφονται τα εβδομαδιαία καθηκοντά μας και τα οποια στην συνέχεια θα συζητιούνται στην συναντησή μας
     

 # Git βασικές εντολές ΑΦΟΥ ΜΑΘΕΤΕ ΠΩΣ ΔΟΥΛΕΥΕΙ ΤΟ GITHUB
 ειναι ο τροπος να δουλεψετε το github απο το τερματικο σας (και μεσα στο VS code)

## 1. Διαμόρφωση (git config)
Ρύθμιση στοιχείων χρήστη (γίνεται μία φορά ανά μηχάνημα).
```bash
git config --global user.name "detandreas"
git config --global user.email "andreaspapathanasiou68@gmail.com"
git config --list
```

## 2. Αρχικοποίηση τοπικού repository
```bash
git init
```

## 3. Έλεγχος αλλαγών (working directory / staging)
```bash
git status
```

## 4. Προσθήκη αρχείων στο staging area
Κάθε φορά που αλλάζεις ένα αρχείο και θέλεις να συμπεριληφθεί στο επόμενο commit πρέπει να το ξαναπροσθέσεις.
```bash
git add .          # προσθέτει όλα τα τροποποιημένα / νέα αρχεία
git add test.py    # προσθέτει συγκεκριμένο αρχείο
```

## 5. Δημιουργία commit με μήνυμα
```bash
git commit -m "first commit"
```

## 6. Κλώνος απομακρυσμένου repository
```bash
git clone https://github.com/detandreas/SaveTheGoverment.git
```

## 7. Branching
```bash
git branch          # λίστα των branch
git checkout -b new-branch   # δημιουργία & μετάβαση σε νέο branch (παλαιότερος τρόπος)
git switch main     # μετάβαση στο main (νεότερη εντολή)
```

## 8. Αποστολή (push) αλλαγών στο remote
Σύνταξη: git push <remote> <branch> (προεπιλογή remote: origin)
```bash
git push origin main
```

## 9. Λήψη (pull) ενημερώσεων από το remote
```bash
git pull
```

## 10. Προβολή ιστορικού / αλλαγών
```bash
git show        # δείχνει τις αλλαγές του τελευταίου commit στο τρέχον branch
```

## 11. Έλεγχος configured remotes
```bash
git remote -v

