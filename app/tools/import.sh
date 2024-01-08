#!/bin/bash


function create_sign() {
	echo "je créé le signe"
	file="file=@$1"
	echo $file
	createSignResponse=$(curl -s -u "$3" -H 'Content-Type: multipart/form-data' -X POST -F $file -F 'data={"name":"'"$2"'"};type=application/json' ${BASE_URL}/ws/sec/signs)
	echo $createSignResponse
	signId=$(jq -r ".signId"  <<< "${createSignResponse}")
echo $signId
	videoId=$(jq -r ".videoId"  <<< "${createSignResponse}")
echo $videoId
	errorMessage=$(jq -r ".errorMessage"  <<< "${createSignResponse}")
echo $errorMessage
	if [ "$errorMessage" != null ]; then
		echo $1 $2 $3 $4 $errorMessage >> $ERRORFILE
	fi
	if [ "$4" != null ] && [ "$signId" != null ]; then
		url=${BASE_URL}/ws/sec/signs/$signId
echo $url
		createDefinitionSignResponse=$(curl -s -u "$3" -H 'Content-Type: multipart/form-data' -X PUT -F 'data={"textDefinition":"'"$4"'"};type=application/json' $url)
		echo $createDefinitionSignResponse
        fi

}

function create_variante_sign() {
	echo "je créé une variante du signe"
	file="file=@$1"
	echo $file
	url=${BASE_URL}/ws/sec/signs/$5/videos
	createVarianteSignResponse=$(curl -s -u "$3" -H 'Content-Type: multipart/form-data' -X POST -F $file -F 'data={"name":"'"$2"'"};type=application/json' $url)
	echo $createVarianteSignResponse
	signId=$(jq -r ".signId"  <<< "${createVarianteSignResponse}")
echo $signId
	videoId=$(jq -r ".videoId"  <<< "${createVarianteSignResponse}")
echo $videoId
	errorMessage=$(jq -r ".errorMessage"  <<< "${createVarianteSignResponse}")
echo $errorMessage
	if [ "$errorMessage" != null ]; then
		echo $1 $2 $3 $4 $errorMessage >> $ERRORFILE
	fi

}

function create_sign_from_request() {
	echo "je créé un signe en réponse à une demande"
	file="file=@$1"
	echo $file
	url=${BASE_URL}/ws/sec/requests/$4/signs
	createSignFromRequestResponse=$(curl -s -u "$3" -H 'Content-Type: multipart/form-data' -X POST -F $file -F 'data={"name":"'"$2"'"};type=application/json' $url)
	echo $createSignFromRequestResponse
	signId=$(jq -r ".signId"  <<< "${createSignFromRequestResponse}")
echo $signId
	videoId=$(jq -r ".videoId"  <<< "${createSignFromRequestResponse}")
echo $videoId
	errorMessage=$(jq -r ".errorMessage"  <<< "${createSignFromRequestResponse}")
echo $errorMessage
	if [ "$errorMessage" != null ]; then
		echo $1 $2 $3 $4 $errorMessage >> $ERRORFILE
	fi
	if [ "$5" != null ] && [ "$signId" != null ]; then
		url=${BASE_URL}/ws/sec/signs/$signId
echo $url
		createDefinitionSignResponse=$(curl -s -u "$3" -H 'Content-Type: multipart/form-data' -X PUT -F 'data={"textDefinition":"'"$5"'"};type=application/json' $url)
		echo $createDefinitionSignResponse
        fi

}

function create_favorite() {
	echo "je créé un favoris"
	url=${BASE_URL}/ws/sec/favorites
	createFavorite=$(curl -s -u "$2" --header 'Content-Type: application/json' --request POST --data '{"name":"'"$1"'"}' $url)
	echo $createFavorite
	favoriteId=$(jq -r ".favoriteId"  <<< "${createFavorite}")
echo $favoriteId
	errorMessage=$(jq -r ".errorMessage"  <<< "${createFavorite}")
echo $errorMessage
	if [ "$errorMessage" != null ]; then
		echo $1 $errorMessage >> $ERRORFILE
	fi

}

function add_sign_to_favorite() {
	echo "j'ajoute le signe au favoris"
	echo favoriteId $1
	echo user $2
	echo videoId $3
	url=${BASE_URL}/ws/sec/favorites/$1
	addSignToFavorite=$(curl -s -u "$2" --header 'Content-Type: application/json' --request PUT --data '{"videoIdToAdd":"'"$3"'"}' $url)
	echo $addSignToFavorite
	errorMessage=$(jq -r ".errorMessage"  <<< "${addSignToFavorite}")
echo $errorMessage
	if [ "$errorMessage" != null ]; then
		echo $1 $errorMessage >> $ERRORFILE
	fi

}

INPUTFILE=$1
INPUT_DIRECTORY=$2
BASE_URL=$3
ERRORFILE=error.csv
rm $ERRORFILE
touch $ERRORFILE


sed 1d $INPUTFILE | while read -r line
do
	echo $line
	file_name="$(cut -d';' -f1 <<<"$line")"
	echo "${file_name}"
	absoluteFileName=${INPUT_DIRECTORY}/"${file_name}"
        if (echo "$file_name" | grep -q ' '); then
		newFileName=$(echo ${file_name// /_})
		cp "$absoluteFileName" "${INPUT_DIRECTORY}/"${newFileName}""
		absoluteFileName=${INPUT_DIRECTORY}/"${newFileName}"
	fi
	echo $absoluteFileName
	if [ ! -f "$absoluteFileName" ]; then
		echo $line "${absoluteFileName}" "file not exist" >> $ERRORFILE
		continue;
	fi
	sign_name="$(cut -d';' -f2 <<<"$line")"
	echo $sign_name

	owner_login="$(cut -d';' -f3 <<<"$line")"
	echo $owner_login
	sign_description="$(cut -d';' -f4 <<<"$line")"
	echo $sign_description
	lists_name="$(cut -d';' -f5 <<<"$line")"
	echo $lists_name
	user=$(echo "$owner_login:DEnis0007")
 searchSign=
response=$(curl -s -u ${user} --data-urlencode "fullname=$sign_name" ${BASE_URL}/ws/sec/signs)
echo RESPONSE : $response
if [ "$response" != "[]" ]; then
	name=$(jq -r ".[] | .name"  <<< "${response}")
	echo $name
	id=$(jq -r ".[] | .id"  <<< "${response}")
	echo $id
	if [ "$name" != "$sign_name" ]; then
		create_sign "$absoluteFileName" "${sign_name}" $user "$sign_description"
	else
		echo "le signe existe déjà"
		create_variante_sign "${absoluteFileName}" "${sign_name}" $user "$sign_description" $id
	fi
else
		create_sign "${absoluteFileName}" "${sign_name}" $user "$sign_description"
fi

while read list_name;
do

echo -"$list_name"-
if [ "${list_name}" ]; then
	url=${BASE_URL}/ws/sec/users/me/favorites
echo $url
  responseSearchFavorite=$(curl -s -u ${user} --data-urlencode "name=$list_name" ${url})
echo RESPONSESEARCHFAVORITE : $responseSearchFavorite
	if [ "$responseSearchFavorite" != "[]" ]; then
		favoriteId=$(jq -r ".[] | .id"  <<< "${responseSearchFavorite}")
		echo $favoriteId
	else
		create_favorite "$list_name" $user
	fi
	if [ "$favoriteId" != null ] && [ "$videoId" != null ]; then
		add_sign_to_favorite $favoriteId $user $videoId
	fi
fi
done < <(echo "$lists_name" | tr ',' '\n')

done
