# hr-marcell-pipeline

## Creating docker images

To create docker images on a host, first-build.sh script needs to be copied
and SSH access to the repository needs to be configured.

```
scp <user>@<server> first-build.sh
ssh <user>@<server>
cd
bash first-build.sh
```

## Running docker-compose

To run a container in a detached mode, run the following

```
ssh <user>@<server>
cd ~/hr-marcell-pipeline
docker-compose up -d all-in-one
```

This will keep the container running even after SSH connection is closed.

Note that services need some time to start up.

## API Docs

Pipeline accepts POST requests on /annotate endpoint.

### POST /annotate

#### Required parameters
_text && metadata_

#### Parameter list
| Name     | Type        | Description                               |
| -------- | ----------- | ----------------------------------------- |
| text     | string      | Value can be either raw text or html.     |
| metadata | json string | Value has to be json-stringifyied object. |

##### Metadata attributes
| Name              | Type   | required/optional | Description                                                                                                        |
| ----------------- | ------ | ----------------- | ------------------------------------------------------------------------------------------------------------------ |
| id                | string | **required**      |                                                                                                                    |
| year              | string | _optional_        |                                                                                                                    |
| title             | string | _optional_        |                                                                                                                    |
| type              | string | _optional_        |                                                                                                                    |
| entype            | string | _optional_        |                                                                                                                    |
| descriptors       | list   | _optional_        | List of objects with the following structure:<br><pre>{<br>    "descriptor": "...",<br>    "tld": "..."<br>}</pre> |
| url               | string | _optional_        |                                                                                                                    |
| in\_effect\_since | string | _optional_        |                                                                                                                    |


#### Response
##### HTTP 200 [OK]
Body contains CONLLUP-formatted string.
##### HTTP 400 [Bad Request]
Request is ill-formatted.
##### HTTP 424 [Failed Dependency]
XLike pipeline returned error.

#### Sample
##### Call
```
curl -X POST -F 'text=Ovaj tekst treba anotirati.' -F 'metadata={"id":"identifikator", "year": "2020", "title": "Testni dokument", "type": "odluka", "entype": "decision", "descriptors": [{"descriptor": "testni deskriptor", "tld": "36"}, {"descriptor": "drugi testni deskriptor", "tld": "36"}], "url": "https://marcell-project.eu", "in_effect_since": "2020"}' http://localhost:8080/annotate
```
##### Response
```
# global.columns = ID FORM LEMMA UPOS XPOS FEATS HEAD DEPREL DEPS MISC MARCELL:NE MARCELL:NP MARCELL:IATE MARCELL:EUROVOC
# newdoc id = identifikator
# date = 2020
# title = Testni dokument
# type = odluka
# entype = decision
# keywords = testni deskriptor | drugi testni deskriptor
# eurovoc = 36
# url = https://marcell-project.eu
# date_effect = 2020

# newpar id = identifikator-p1
# sent_id = identifikator-p1s1
# text = Ovaj tekst treba anotirati.
1   Ovaj    ovaj    _   P   _   2   Atr _   _   O   _   _   _
2   tekst   tekst   _   N   _   3   Obj _   _   O   _   _   _
3   treba   trebati _   V   _   0   Pred    _   _   O   _   _   _
4   anotirati   anotirati   _   V   _   3   Obj _   SpaceAfter=No   O   _   _   _
5   .   .   _   Z   _   0   AuxK    _   _   O   _   _   _
```

## TODO

* add log rotation
