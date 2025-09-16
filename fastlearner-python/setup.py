from setuptools import setup, find_packages

setup(
    name='fastlearnerpythonservice',
    version='1.0.0',
    packages=find_packages(),
    install_requires=[
        'llama_index',
        'langchain',
        'pydub',
        'virtualenv',
        'flask',
        'api',
        'resources',
        'pypdf',
        'psycopg2',
        'mlxtend',
    ],
    dependency_links=[
        'git+https://github.com/openai/whisper.git#egg=whisper',
    ],
)
