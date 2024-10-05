#! /bin/sh

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:./src/main/bin:"$PATH"

verbose=false

while getopts ":v" opt; do
  case $opt in
    v)
      verbose=true
      ;;
    *)
      echo "Option invalide: -$OPTARG" >&2
      exit 1
      ;;
  esac
done

for i in ./src/test/deca/codegen/valid/*/*.deca
do
    echo "----------------------------------------"
    echo "Test de $(basename "$i")"

    base_name=$(basename "$i" .deca)
    j="$(dirname "$i")/${base_name}.ass"
    rm -f "$j" 2>/dev/null
    decac "$i" || exit 1
    if [ ! -f "$j" ]; then
        echo "Fichier ${base_name}.ass non généré."
        exit 1
    fi

    resultat=$(echo "1\\n2\\n3\\n4\\n5\\n50\\n47" | ima "$j")
    rm -f "$j"

    attendu=$(awk '/\/\/ Resultats:/{flag=1; next} /\/\/ FIN Resultats/{flag=0} flag' "$i" | sed -e 's/\/\/[[:space:]]*//')


    if [ "$resultat" = "$attendu" ]; then
        if [ "$verbose" = true ]; then
            echo "Résultat attendu:"
            echo "$attendu"
        else
            echo "Résultat attendu"
        fi
    else
        if [ "$verbose" = true ]; then
            echo "Résultat inattendu:"
            echo "$resultat"
        else
            echo "Résultat inattendu"
        fi
        echo "Résultat espéré:"
        echo "$attendu"
    fi
done

