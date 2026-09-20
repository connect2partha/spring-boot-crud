{{- define "spring-boot-crud.name" -}}
{{- .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "spring-boot-crud.fullname" -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "spring-boot-crud.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/name: {{ include "spring-boot-crud.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "spring-boot-crud.selectorLabels" -}}
app.kubernetes.io/name: {{ include "spring-boot-crud.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
