import json

from bs4 import BeautifulSoup
from flask import Flask, request
from io import StringIO
from werkzeug.exceptions import FailedDependency, BadRequest

from .annotate import setup_annotator, format_output


app = Flask(__name__)
app.config.from_json('.config')

annotate = setup_annotator(app.config["ANNOTATION_CONFIG"], app.logger, verbose_output=app.debug)


@app.route('/annotate', methods=['POST'])
def main():
    text = request.form.get('text')
    is_html = bool(BeautifulSoup(text, "html.parser").find())
    metadata = request.form.get('metadata')
    try:
        metadata = json.loads(metadata)
        metadata['id']
    except (json.JSONDecodeError, KeyError):
        raise BadRequest()

    try:
        out = format_output(annotate(text, is_html), metadata, StringIO()).getvalue()
    except Exception:
        raise FailedDependency()

    return out
