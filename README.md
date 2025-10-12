# SaveTheGoverment
Εδώ θα αναπτυξουμε την εργασια μας στο μαθημα ΠΡΟΓΡΑΜΜΑΤΙΣΜΟΣ ΙΙ

## Συντονισμός
- **Συναντηση** (διαδικτυακή ή φυσική) μια φορά την εβδομάδα,οπου θα αναλύουμε πρόοδο και τα επομενά μας βήματα
- **Εξοικείωση με το github** δειτε κανα βιντεακι στο youtube πχ https://www.youtube.com/watch?v=a9u2yZvsqHA ωστε να καταλάβετε πως λειτουργεί και τι προσφέρει.Να ξέρετε σίγουρα τι είναι :Repository,Branch,Pull Request(PR),Commit,Merge
- **Εξοικείωση με το git** Yπάρχουν σημειώσεις για το git στο moodle στις σημειώσεις φροντιστηρίου (φροντιστήριο 2).Θα μας βοηθήσει να χρησιμοποιούμε το github απο το command line επιταχύνοντας την ανάπτυξη του κώδικα.

## Οταν αρχίσουμε να γράφουμε κώδικα
- αυτό το αρχείο που διαβάζετε θα περιγράφει το πως λειτουργεί η εφαρμογη
- Θα φτιάξω ενα αλλο αρχείο (εκτος αν το αναλάβει κάποιος αλλος) ίδιου τύπου οπου θα περιγράφονται τα εβδομαδιαία καθηκοντά μας και τα οποια στην συνέχεια θα συζητιούνται στην συναντησή μας

---

## Usage
```bash
mvn javafx:run      # Αν έχετε εγκαταστήσει στον υπολογιστή σας το maven
./mvnw javafx:run   # wrapper του maven το εχετε στον υπολογιστη σας κανοντας clone το repository μας
```

---

# Team git Workflow - SaveTheGovernmet
## Στόχος : Αποφυγή conflicts και χαοτικού κώδικα 

## 1. Προστατευμένο `main` Branch
- Δεν γίνονται **άμεσα commits** στο `main`.
- Κάθε αλλαγή περνά από **Pull Request (PR)**.

## 2. Συγχρονισμός με main
κάθε μέρα πριν ξεκινήσεις δουλειά:
```bash 
git checkout main   # switch στο main branch
git pull origin main    # κατέβασμα νέου κωδικα
git checkout -b feature/name    # δημιουργία προσωπικού branch οπου θα δουλέψεις ενα συγκεκριμένο feature
git rebase main     # ευθυγράμμιση με την main
git merge main  # εναλλακτικα αν δεν θελετε rebase
```

## 3. Προσωπικά Branches Ανά Μέλος
Δουλεύουμε πάντα σε νέο branch
Παραδείγματα ονομάτων:
```bash
git checkout -b feature/ui-sidebar  # υποδηλώνουμε οτι στο branch προσθέτουμε νεα λειτουργια --> ui-sidebar
git checkout -b bugfix/ui-overlap   # υποδηλώνουμε οτι στο branch διορθώνουμε ενα συγκεκριμένο bug --> ui-overlap
```
## 4. Μικρά και Καθαρά Commits
Κάνε commit ανά λογικό βήμα:πχ Ολοκλήρωσες ενα feature,διόρθωσες ενα bug,βελτίωσες υπάρχον κωδικα κτλπα
```bash
git add .
git commit -m "feat: add budget summary table"
```
prefix examples:
- **feat** --> νέα λειτουργία
- **fix** --> διόρθωση bug
- **refactor** --> βελτίωση κώδικα χωρίς αλλαγή λειτουργίας
- **docs** --> αλλαγή σε documentation

## 5. Code Review & Pull Requests
- Κάθε PR εγκρίνεται τουλάχστιον από ένα άτομο.
- Έλεγχος για:
    * Λειτουργικότητα
    * conflicts
    * καθαρότητα/αναγνωσιμότητα κώδικα
    * αποδοτικότητα κωδικα

## 6. Ανόηση Περιττών Αρχείων
Φτιαξτε .gitignore αρχειο στο οποιο συμπεριλάβετε τα αρχεια που δεν θέλετε να γινονται push στο remote repository
```bash
target/
.class
.vscode/
```

## 7. Ενιαίο περιβάλλον Ανάπτυξης
- Ίδιες εκδόσεις Java/Maven
- Κοινό pom.xml με όλες τις εξαρτήσεις

---


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
git branch 'branch-name'    # δημιουργια branch
git checkout -b "new-branch"   # δημιουργία & μετάβαση σε νέο branch (παλαιότερος τρόπος)
git switch main     # μετάβαση στο main (νεότερη εντολή)
```

## 8. Διαγραφη branch
``` bash
git branch -d "branch-to-delete"    # διαγραφη branch που δεν εχει γινει merged.Για τοπικα branches
git branch -d   # διαγραφη branch που εχει γινει fully merged.Για τοπικα branches
git push origin --delete "branch-name" # διαγραφει απομακρυσμενα branches (στο github)
```

## 9. Αποστολή (push) αλλαγών στο remote
Σύνταξη: git push <remote> <branch> (προεπιλογή remote: origin)
```bash
git push origin main
```

## 10. Λήψη (pull) ενημερώσεων από το remote
```bash
git pull
```

## 11. Προβολή ιστορικού / αλλαγών
```bash
git show        # δείχνει τις αλλαγές του τελευταίου commit στο τρέχον branch
```

## 12. Έλεγχος configured remotes
```bash
git remote -v
```

## 13. Merge branch
```bash
git ckeckout main # αλλαγη στο branch στο οποιο θες να κανεις το merge
git merge "branch-name" # εγινε merge το branch-name --> main
git branch -d # διαγραφη του fully merged branch
```

