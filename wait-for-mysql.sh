#!/bin/sh
#!/bin/sh

echo "⏳ Attente que MySQL (mysql-db:3306) soit prêt..."

while ! nc -z mysql-db 3306; do
  echo "❌ MySQL pas encore prêt, nouvelle tentative dans 3 secondes..."
  sleep 3
done

echo "✅ MySQL est prêt. Lancement de l'application Spring Boot..."
exec java -jar app.jar
