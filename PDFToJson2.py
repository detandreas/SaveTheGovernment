import pandas as pd
import tabula
import json
from pathlib import Path

def merge_rows(df: pd.DataFrame, start: int, stop: int):
    rows = df.iloc[start:stop]
    merged = rows.apply(lambda col: ' '.join(col.dropna().astype(str)), axis=0)
    df.iloc[start] = merged
    df = df.drop(index=list(range(start+1, stop+1))).reset_index(drop=True)
    return df

def process_pdf(file_path: str):
    dfs = tabula.read_pdf(file_path, multiple_tables=False, lattice=False, pages=1)
    df = dfs[0]
    if 'Ευρώ' in df.columns:
        del df['Ευρώ']
    df = df.rename(columns={df.columns[0]: 'REVENUE',
                            df.columns[1]: 'ID',
                            df.columns[2]: 'BILL',
                            df.columns[3]: 'VALUE'})
    # Συγκεκριμένες συγχωνεύσεις για το layout του 2025 PDF
    df = merge_rows(df, 28, 30)
    df = merge_rows(df, 8, 10)

    df['ID'] = pd.to_numeric(df['ID'], errors='coerce').fillna(0).astype(int)
    df['VALUE'] = df['VALUE'].astype(str).str.replace('.', '', regex=False)
    df['VALUE'] = pd.to_numeric(df['VALUE'], errors='coerce').fillna(0.0)

    esoda = df.iloc[0:13][['ID', 'BILL', 'VALUE']]
    eksoda = df.iloc[15:][['ID', 'BILL', 'VALUE']]
    return esoda, eksoda

def update_budget_json(out_path: str, year: int, esoda: pd.DataFrame, eksoda: pd.DataFrame):
    out = Path(out_path)
    if out.exists():
        with out.open('r', encoding='utf-8') as f:
            budget = json.load(f)
    else:
        budget = {}

    # Μετατροπή σε list[dict]
    budget[str(year)] = {
        "esoda": esoda.to_dict(orient="records"),
        "eksoda": eksoda.to_dict(orient="records")
    }

    with out.open('w', encoding='utf-8') as f:
        json.dump(budget, f, ensure_ascii=False, indent=2)

def main():
    inputs = [
        ("Κρατικός-Προϋπολογισμός-2025_ΟΕ.pdf", 2025)
    ]
    out_file = "budget.json"

    for file_path, year in inputs:
        esoda, eksoda = process_pdf(file_path)
        update_budget_json(out_file, year, esoda, eksoda)

if __name__ == "__main__":
    main()
