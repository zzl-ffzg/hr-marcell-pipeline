from setuptools import setup

setup(
    name="annotator",
    version="0.0.1",
    packages=["annotator"],
    include_package_data=True,
    dependency_links=[
        "git+https://github.com/zzl-ffzg/reldi-tokeniser.git@v1.0.1"
    ]
)
