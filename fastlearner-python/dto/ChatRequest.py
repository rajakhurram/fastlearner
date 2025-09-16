from typing import Optional
import json
from dataclasses import dataclass
from collections import namedtuple
from json import JSONEncoder


@dataclass
class ChatRequest:
    transcript: Optional[str]
    question: Optional[str]
