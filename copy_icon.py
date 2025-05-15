import os
import shutil

# Пути к директориям
source_file = "assets/ship.png"
target_dirs = [
    "android/res/drawable-hdpi",
    "android/res/drawable-mdpi",
    "android/res/drawable-xhdpi",
    "android/res/drawable-xxhdpi",
    "android/res/drawable-xxxhdpi"
]

# Проверяем наличие исходного файла
if not os.path.exists(source_file):
    print(f"Исходный файл не найден: {source_file}")
    exit(1)

# Копируем файл в каждую директорию
for target_dir in target_dirs:
    # Убедимся, что директория существует
    os.makedirs(target_dir, exist_ok=True)
    
    target_file = os.path.join(target_dir, "ic_launcher.png")
    try:
        shutil.copy2(source_file, target_file)
        print(f"Успешно скопировано в {target_file}")
    except Exception as e:
        print(f"Ошибка при копировании в {target_file}: {e}")

print("Готово!") 