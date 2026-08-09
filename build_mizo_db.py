import sqlite3
import os

def build_mizo_db(input_json_path, output_db_path):
    # Ensure we are not accidentally treating the db as text
    if os.path.exists(output_db_path):
        os.remove(output_db_path)

    # sqlite3.connect creates the file in binary mode automatically
    conn = sqlite3.connect(output_db_path)
    cursor = conn.cursor()

    cursor.execute('''
        CREATE TABLE IF NOT EXISTS verses (
            book_id INTEGER,
            chapter INTEGER,
            verse INTEGER,
            text TEXT,
            translation_code TEXT
        )
    ''')

    # Example data insertion logic
    # ...

    conn.commit()
    conn.close()

    print(f"Successfully created {output_db_path} in binary mode.")

if __name__ == "__main__":
    # When serving or copying the file, ensure binary mode is used:
    # with open('MIZO.db', 'rb') as f:
    #     data = f.read()
    build_mizo_db('mizo.json', 'MIZO.db')
