package com.wanderlust.community_antiepidemic_system.entity

data class Country(var province: List<Province> = mutableListOf())

data class Province(var name: String, var city: List<City> = mutableListOf())

data class City(var name: String, var area: List<String> = mutableListOf())