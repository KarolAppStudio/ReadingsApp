import sqlite3
import re
from collections import Counter

db_path = 'app/src/main/assets/bibles.db'
conn = sqlite3.connect(db_path)
cursor = conn.cursor()

def fix_text(text, code):
    if not text:
        return text

    # 1. Punctuation followed by non-space
    new_text = re.sub(r'([.!?;|:,])(?=[^\s0-9”"\'\)\]\}])', r'\1 ', text)

    # 2. Closing quote/bracket followed by word character
    new_text = re.sub(r'([”"\'\)\]\}])(?=[\w])', r'\1 ', new_text)

    if code == 'TEL':
        # 3. Telugu specific fusions (Longer words to avoid splitting names/verbs)
        start_words = ['దేవుడు', 'మరియు', 'యెహోవా', 'యేసు', 'ప్రభువు', 'ఇశ్రాయేలు']
        for word in start_words:
            # Add space before word if preceded by a Telugu character
            new_text = re.sub(r'([\u0C00-\u0C7F])(' + word + r')', r'\1 \2', new_text)
            # Add space after word if followed by a Telugu character
            new_text = re.sub(r'(' + word + r')([\u0C00-\u0C7F])', r'\1 \2', new_text)

        # Specific verb fusions
        new_text = re.sub(r'న్నానునా', 'న్నాను నా', new_text)
        new_text = re.sub(r'బలముకలిగెను', 'బలము కలిగెను', new_text)
        new_text = re.sub(r'కలిగెనునీ', 'కలిగెను నీ', new_text) # specifically for 1 Sam 2:1

    # Clean up double spaces
    new_text = re.sub(r' +', ' ', new_text)

    return new_text

cursor.execute("SELECT translation_code, book_id, chapter, verse, text FROM verses")
rows = cursor.fetchall()

updates = []
for code, bid, chap, v, text in rows:
    new_text = fix_text(text, code)
    if new_text != text:
        updates.append((new_text, code, bid, chap, v))

print(f"Total updates: {len(updates)}")

if updates:
    cursor.executemany(
        "UPDATE verses SET text = ? WHERE translation_code = ? AND book_id = ? AND chapter = ? AND verse = ?",
        updates
    )
    conn.commit()
    print("Database updated successfully.")

conn.close()
