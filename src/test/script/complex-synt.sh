#! /bin/sh
cd "$(dirname "$0")"/../../.. || exit 1
PATH=./src/test/script/launchers:"$PATH"

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

echo "TESTS INVALIDES"

for i in ./src/test/deca/syntax/invalid/*/*.deca
do
    echo "----------------------------------------"
    echo "Test de $(basename "$i")"
    if [ "$verbose" = true ]; then
        cat "$i" | test_synt
    fi
    if test_synt "$i" 2>&1 | grep -q -e "$(basename "$i"):[0-9]*:"
    then 
        echo "Echec attendu pour test_synt"
    else
        echo "Succes inattendu de test_synt"
    fi
done
echo "==========================================="
echo "TESTS VALIDES"

for i in ./src/test/deca/syntax/valid/*/*.deca
do    
    echo "----------------------------------------"
    echo "Test de $(basename "$i")"
    if [ "$verbose" = true ]; then
      cat "$i" | test_synt
    fi
    if test_synt "$i" 2>&1 | grep -q -e "$(basename "$i"):[0-9]*:"
    then
        echo "Echec inattendu pour test_synt"
    else
        echo "Succes attendu de test_synt"
    fi
done